package com.octopus.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

/**
 * Defines a service used to extract the body from a Lambda request.
 */
public interface RequestBodyExtractor {

  /**
   * Return the request body.
   *
   * @param input The HTTP request.
   * @return The request body.
   */
  String getBody(final APIGatewayProxyRequestEvent input);
}
