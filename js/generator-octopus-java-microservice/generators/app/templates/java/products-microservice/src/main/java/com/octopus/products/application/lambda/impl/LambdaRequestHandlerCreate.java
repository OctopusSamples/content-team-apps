
package com.octopus.products.application.lambda.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.net.HttpHeaders;
import com.octopus.Constants;
import com.octopus.exceptions.InvalidInputException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.lambda.ApiGatewayProxyResponseEventWithCors;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.ProxyResponseBuilder;
import com.octopus.lambda.RequestBodyExtractor;
import com.octopus.lambda.RequestMatcher;
import com.octopus.products.application.Paths;
import com.octopus.products.application.lambda.LambdaRequestHandler;
import com.octopus.products.domain.handlers.ResourceHandlerCreate;
import io.vavr.control.Try;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;

/**
 * Handle entity creation requests.
 */
@ApplicationScoped
public class LambdaRequestHandlerCreate implements LambdaRequestHandler {

  /**
   * A regular expression matching the collection of entities.
   */
  public static final Pattern ROOT_RE = Pattern.compile(Paths.API_ENDPOINT + "/?");

  @Inject
  RequestMatcher requestMatcher;

  @Inject
  ResourceHandlerCreate resourceHandler;

  @Inject
  LambdaHttpHeaderExtractor lambdaHttpHeaderExtractor;

  @Inject
  RequestBodyExtractor requestBodyExtractor;

  @Inject
  ProxyResponseBuilder proxyResponseBuilder;

  /**
   * Handle the lambda request.
   *
   * @param input The request event.
   * @return A populated response event, or an empty optional if this service did not handle the event.
   */
  @Override
  public Optional<APIGatewayProxyResponseEvent> handleRequest(@NonNull final APIGatewayProxyRequestEvent input) {

    if (!requestMatcher.requestIsMatch(input, ROOT_RE, Constants.Http.POST_METHOD)) {
      return Optional.empty();
    }

    return Try.of(() -> Optional.of(
            new ApiGatewayProxyResponseEventWithCors()
                .withStatusCode(201)
                .withBody(
                    resourceHandler.create(
                        requestBodyExtractor.getBody(input),
                        lambdaHttpHeaderExtractor.getAllHeaders(input, Constants.DATA_PARTITION_HEADER),
                        lambdaHttpHeaderExtractor.getFirstHeader(input, HttpHeaders.AUTHORIZATION).orElse(null),
                        lambdaHttpHeaderExtractor.getFirstHeader(input, Constants.SERVICE_AUTHORIZATION_HEADER).orElse(null)))))
        .recover(UnauthorizedException.class, e -> Optional.of(proxyResponseBuilder.buildUnauthorizedRequest(e)))
        .recover(InvalidInputException.class, e -> Optional.of(proxyResponseBuilder.buildBadRequest(e)))
        .onFailure(Throwable::printStackTrace)
        .recover(e -> Optional.of(proxyResponseBuilder.buildError(e, requestBodyExtractor.getBody(input))))
        .get();
  }
}
