
package com.octopus.products.application.lambda.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.octopus.Constants.Http;
import com.octopus.lambda.ApiGatewayProxyResponseEventWithCors;
import com.octopus.products.application.lambda.LambdaRequestHandler;
import io.vavr.control.Try;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import lombok.NonNull;

/**
 * Handle CORS requests.
 */
@ApplicationScoped
public class LambdaRequestHandlerCors implements LambdaRequestHandler {

  /**
   * Handle the lambda request.
   *
   * @param input The request event.
   * @return A populated response event, or an empty optional if this service did not handle the event.
   */
  @Override
  public Optional<APIGatewayProxyResponseEvent> handleRequest(@NonNull final APIGatewayProxyRequestEvent input) {

    if (!Http.OPTIONS_METHOD.equalsIgnoreCase(input.getHttpMethod())) {
      return Optional.empty();
    }

    return Try.of(() -> Optional.of(new ApiGatewayProxyResponseEventWithCors().withStatusCode(201))).get();
  }
}
