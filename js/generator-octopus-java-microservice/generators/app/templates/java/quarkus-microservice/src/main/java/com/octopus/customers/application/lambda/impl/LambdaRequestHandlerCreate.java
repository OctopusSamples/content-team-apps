
package com.octopus.customers.application.lambda.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.net.HttpHeaders;
import com.octopus.Constants;
import com.octopus.customers.application.Paths;
import com.octopus.customers.application.lambda.LambdaRequestHandler;
import com.octopus.customers.domain.handlers.ResourceHandler;
import com.octopus.exceptions.InvalidInputException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.lambda.ApiGatewayProxyResponseEventWithCors;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.ProxyResponseBuilder;
import com.octopus.lambda.RequestBodyExtractor;
import com.octopus.lambda.RequestMatcher;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
  ResourceHandler resourceHandler;

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
  public Optional<APIGatewayProxyResponseEvent> handleRequest(
      APIGatewayProxyRequestEvent input) {
    try {
      if (!requestMatcher.requestIsMatch(input, ROOT_RE, Constants.Http.POST_METHOD)) {
        return Optional.empty();
      }

      return Optional.of(
          new ApiGatewayProxyResponseEventWithCors()
              .withStatusCode(201)
              .withBody(
                  resourceHandler.create(
                      requestBodyExtractor.getBody(input),
                      lambdaHttpHeaderExtractor.getAllHeaders(input,
                          Constants.DATA_PARTITION_HEADER),
                      lambdaHttpHeaderExtractor.getFirstHeader(input, HttpHeaders.AUTHORIZATION)
                          .orElse(null),
                      lambdaHttpHeaderExtractor.getFirstHeader(input,
                          Constants.SERVICE_AUTHORIZATION_HEADER).orElse(null))));

    } catch (final UnauthorizedException e) {
      return Optional.of(proxyResponseBuilder.buildUnauthorizedRequest(e));
    } catch (final InvalidInputException e) {
      return Optional.of(proxyResponseBuilder.buildBadRequest(e));
    } catch (final Exception e) {
      e.printStackTrace();
      return Optional.of(proxyResponseBuilder.buildError(e, requestBodyExtractor.getBody(input)));
    }
  }
}
