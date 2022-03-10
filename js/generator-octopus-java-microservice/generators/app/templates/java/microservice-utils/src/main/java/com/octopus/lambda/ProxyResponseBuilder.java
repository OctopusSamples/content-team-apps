package com.octopus.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import lombok.NonNull;

/**
 * Service for building common responses.
 */
public interface ProxyResponseBuilder {

  /**
   * Build an error object including the exception name. https://jsonapi.org/format/#error-objects
   *
   * @param ex The exception
   * @return The ProxyResponse representing the error.
   */
  APIGatewayProxyResponseEvent buildError(Exception ex);

  /**
   * Build an error object including the exception name and the body of the request that was sent.
   * https://jsonapi.org/format/#error-objects
   *
   * @param ex          The exception
   * @param requestBody The request body
   * @return The ProxyResponse representing the error.
   */
  APIGatewayProxyResponseEvent buildError(Exception ex, String requestBody);

  /**
   * Build a error object for a 404 not found error. https://jsonapi.org/format/#error-objects
   *
   * @return The ProxyResponse representing the error.
   */
  APIGatewayProxyResponseEvent buildNotFound();

  /**
   * Build an error object including the exception name. https://jsonapi.org/format/#error-objects
   *
   * @param ex The exception
   * @return The ProxyResponse representing the error.
   */
  APIGatewayProxyResponseEvent buildBadRequest(Exception ex);

  /**
   * Build an error object including the exception name. https://jsonapi.org/format/#error-objects
   *
   * @param ex The exception
   * @return The ProxyResponse representing the error.
   */
  APIGatewayProxyResponseEvent buildUnauthorizedRequest(Exception ex);
}
