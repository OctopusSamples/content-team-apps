package com.octopus.products.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.octopus.lambda.ProxyResponseBuilder;
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

  /**
   * See https://github.com/quarkusio/quarkus/issues/5811 for why we need @Transactional.
   *
   * @param input   The request details
   * @param context The request context
   * @return The proxy response
   */
  @Override
  @Transactional
  public APIGatewayProxyResponseEvent handleRequest(
      @NonNull final APIGatewayProxyRequestEvent input, @NonNull final Context context) {

    /*
     Lambdas don't enjoy the same middleware and framework support as web servers (although
     https://quarkus.io/guides/amazon-lambda-http is looking like a good option when it
     comes out of preview), so we are on our own with functionality such as routing requests to
     handlers. This code simply calls each handler to find the first one that responds to the request.
    */
    return handlers.stream()
        // handle the request
        .map(h -> h.handleRequest(input))
        // we're only interested in populated responses
        .filter(Optional::isPresent)
        // get the response
        .map(Optional::get)
        // we only expect one of the handlers to provide a response, so get the first one
        .findFirst()
        // otherwise nothing handled the response, and we return a 404
        .orElseGet(() -> proxyResponseBuilder.buildNotFound());
  }
}
