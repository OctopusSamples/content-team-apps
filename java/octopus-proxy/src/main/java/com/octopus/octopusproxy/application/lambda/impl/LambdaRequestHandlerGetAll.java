package com.octopus.octopusproxy.application.lambda.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.net.HttpHeaders;
import com.octopus.Constants;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.lambda.ApiGatewayProxyResponseEventWithCors;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import com.octopus.lambda.ProxyResponseBuilder;
import com.octopus.lambda.RequestMatcher;
import com.octopus.octopusproxy.application.Paths;
import com.octopus.octopusproxy.application.lambda.LambdaRequestHandler;
import com.octopus.octopusproxy.domain.handlers.ResourceHandler;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Handle entity lookup requests.
 */
@ApplicationScoped
public class LambdaRequestHandlerGetAll implements LambdaRequestHandler {

  /**
   * A regular expression matching a single entity.
   */
  public static final Pattern COLLECTION_RE = Pattern.compile(Paths.API_ENDPOINT + "/?");

  @Inject
  RequestMatcher requestMatcher;

  @Inject
  ResourceHandler resourceHandler;

  @Inject
  LambdaHttpHeaderExtractor lambdaHttpHeaderExtractor;

  @Inject
  LambdaHttpValueExtractor lambdaHttpValueExtractor;

  @Inject
  ProxyResponseBuilder proxyResponseBuilder;

  /**
   * Handle the lambda request.
   *
   * @param input The request event.
   * @return A populated response event, or an empty optional if this service did not handle the
   *     event.
   */
  @Override
  public Optional<APIGatewayProxyResponseEvent> handleRequest(
      APIGatewayProxyRequestEvent input) {
    try {

      if (!requestMatcher.requestIsMatch(input, COLLECTION_RE, Constants.Http.GET_METHOD)) {
        return Optional.empty();
      }

      final String entity =
          resourceHandler.getAll(
              lambdaHttpValueExtractor.getQueryParam(input, "apiKey").orElse(""),
              lambdaHttpValueExtractor.getQueryParam(input, "filter").orElse(""),
              lambdaHttpHeaderExtractor.getAllHeaders(input, Constants.DATA_PARTITION_HEADER),
              lambdaHttpHeaderExtractor.getFirstHeader(
                      input,
                      HttpHeaders.AUTHORIZATION)
                  .orElse(null),
              lambdaHttpHeaderExtractor.getFirstHeader(
                      input,
                      Constants.SERVICE_AUTHORIZATION_HEADER)
                  .orElse(null));

      return Optional.of(
          new ApiGatewayProxyResponseEventWithCors().withStatusCode(200).withBody(entity));


    } catch (final UnauthorizedException e) {
      return Optional.of(proxyResponseBuilder.buildUnauthorizedRequest(e));
    } catch (final EntityNotFoundException ex) {
      return Optional.of(proxyResponseBuilder.buildNotFound());
    } catch (final Exception e) {
      e.printStackTrace();
      return Optional.of(proxyResponseBuilder.buildError(e));
    }
  }
}
