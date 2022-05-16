package com.octopus.products.application.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Optional;

/**
 * Defines a service that can optionally handle a Lambda request.
 */
public interface LambdaRequestHandler {

  /**
   * Handle the lambda request.
   *
   * @param input The request event.
   * @return A populated response event, or an empty optional if this service did not handle the event.
   */
  Optional<APIGatewayProxyResponseEvent> handleRequest(final APIGatewayProxyRequestEvent input);
}
