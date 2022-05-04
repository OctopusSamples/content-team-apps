package com.octopus.customers.application.lambda.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.net.HttpHeaders;
import com.octopus.Constants;
import com.octopus.customers.application.Paths;
import com.octopus.customers.application.lambda.LambdaRequestHandler;
import com.octopus.customers.domain.handlers.ResourceHandler;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.lambda.ApiGatewayProxyResponseEventWithCors;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.ProxyResponseBuilder;
import com.octopus.lambda.RequestMatcher;
import com.octopus.utilties.RegExUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Handle entity lookup requests.
 */
@ApplicationScoped
public class LambdaRequestHandlerGetOne implements LambdaRequestHandler {

  /**
   * A regular expression matching a single entity.
   */
  public static final Pattern INDIVIDUAL_RE = Pattern.compile(Paths.API_ENDPOINT + "/(?<id>\\d+)");

  @Inject
  RequestMatcher requestMatcher;

  @Inject
  ResourceHandler resourceHandler;

  @Inject
  LambdaHttpHeaderExtractor lambdaHttpHeaderExtractor;

  @Inject
  RegExUtils regExUtils;

  @Inject
  ProxyResponseBuilder proxyResponseBuilder;

  /**
   * Handle the lambda request.
   *
   * @param input The request event.
   * @return A populated response event, or an empty optional if this service did not handle the
   * event.
   */
  @Override
  public Optional<APIGatewayProxyResponseEvent> handleRequest(
      APIGatewayProxyRequestEvent input) {
    try {

      if (!requestMatcher.requestIsMatch(input, INDIVIDUAL_RE, Constants.Http.GET_METHOD)) {
        return Optional.empty();
      }

      final Optional<String> id = regExUtils.getGroup(INDIVIDUAL_RE, input.getPath(), "id");

      if (id.isPresent()) {
        final String entity =
            resourceHandler.getOne(
                id.get(),
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
      }
      return Optional.of(proxyResponseBuilder.buildNotFound());

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
