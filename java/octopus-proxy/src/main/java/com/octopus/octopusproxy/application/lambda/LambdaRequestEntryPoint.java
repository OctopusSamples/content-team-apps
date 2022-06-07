package com.octopus.octopusproxy.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.exceptions.InvalidFilterException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.lambda.ProxyResponseBuilder;
import io.quarkus.logging.Log;
import io.vavr.control.Try;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import lombok.NonNull;

/**
 * The Lambda entry point used to return resources.
 */
@Named("ResourceHandler")
@ApplicationScoped
public class LambdaRequestEntryPoint implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @Inject
  ProxyResponseBuilder proxyResponseBuilder;

  @Inject
  Instance<LambdaRequestHandler> handlers;

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  /**
   * Handle the lambda proxy request.
   *
   * @param input   The request details
   * @param context The request context
   * @return The proxy response
   */
  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      @NonNull final APIGatewayProxyRequestEvent input, @NonNull final Context context) {

    /*
     Lambdas don't enjoy the same middleware and framework support as web servers (although
     https://quarkus.io/guides/amazon-lambda-http is looking like a good option when it
     comes out of preview), so we are on our own with functionality such as routing requests to
     handlers. This code simply calls each handler to find the first one that responds to the request.
    */
    return Try.of(() -> handlers.stream()
            // handle the request
            .map(h -> h.handleRequest(input))
            // we're only interested in populated responses
            .filter(Optional::isPresent)
            // get the response
            .map(Optional::get)
            // we only expect one of the handlers to provide a response, so get the first one
            .findFirst()
            // otherwise nothing handled the response, and we return a 404
            .orElseGet(() -> proxyResponseBuilder.buildPathNotFound()))
        /*
          We don't expect anything other than a 200 OK, even if the result is empty. It is possible that the request
          was for a missing entity, had a bad filter, or was unauthorized, but if the front end is working as expected,
          these events should be rare. Log the event so we can track down any issues between the frontend and this service
          later on.
         */
        .onFailure(e -> Log.warn(microserviceNameFeature.getMicroserviceName() + "-General-UnexpectResponse", e))
        // Map a EntityNotFoundException to a "not found" response
        .recover(EntityNotFoundException.class, proxyResponseBuilder.buildNotFound())
        // Map a UnauthorizedException to a "unauthorized" response
        .recover(UnauthorizedException.class, ex -> proxyResponseBuilder.buildUnauthorizedRequest(ex))
        // Map the InvalidFilterException to a bad request response
        .recover(InvalidFilterException.class, ex -> proxyResponseBuilder.buildBadRequest(ex))
        // Any other failures represent a more significant issue, so log them as an error
        .onFailure(e -> Log.error(microserviceNameFeature.getMicroserviceName() + "-General-GeneralError", e))
        // All other exceptions are treated as server side exceptions
        .recover(Exception.class, ex -> proxyResponseBuilder.buildError(ex))
        // Get the result, or rethrow the exception
        .get();
  }
}
