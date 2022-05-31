package com.octopus.lambda.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.octopus.lambda.ApiGatewayProxyResponseEventWithCors;
import com.octopus.lambda.ProxyResponseBuilder;
import lombok.NonNull;
import org.apache.commons.text.StringEscapeUtils;

/**
 * Utility class for building common responses.
 */
public class ProxyResponseBuilderImpl implements ProxyResponseBuilder {

  @Override
  public APIGatewayProxyResponseEvent buildError(@NonNull final Throwable ex) {
    return new ApiGatewayProxyResponseEventWithCors()
        .withStatusCode(500)
        .withBody("{\"errors\": [{\"code\": \"" + ex.getClass().getCanonicalName() + "\"}]}");
  }

  @Override
  public APIGatewayProxyResponseEvent buildError(@NonNull final Throwable ex,
      @NonNull final String requestBody) {
    return new ApiGatewayProxyResponseEventWithCors()
        .withStatusCode(500)
        .withBody("{\"errors\": [{\"code\": \""
            + ex.getClass().getCanonicalName()
            + "\", \"meta\": {\"requestBody\": \""
            + StringEscapeUtils.escapeJson(requestBody)
            + "\"}}]}");
  }

  @Override
  public APIGatewayProxyResponseEvent buildNotFound() {
    return new ApiGatewayProxyResponseEventWithCors()
        .withStatusCode(404)
        .withBody("{\"errors\": [{\"title\": \"Resource not found\"}]}");
  }

  @Override
  public APIGatewayProxyResponseEvent buildBadRequest(@NonNull final Throwable ex) {
    return new ApiGatewayProxyResponseEventWithCors()
        .withStatusCode(400)
        .withBody("{\"errors\": [{\"code\": \"" + ex.getClass().getCanonicalName() + "\"}]}");
  }

  @Override
  public APIGatewayProxyResponseEvent buildUnauthorizedRequest(@NonNull final Throwable ex) {
    return new ApiGatewayProxyResponseEventWithCors()
        .withStatusCode(403)
        .withBody("{\"errors\": [{\"title\": \"Unauthorized\"}]}");
  }
}
