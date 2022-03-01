package com.octopus.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.HashMap;

/**
 * A wrapper around APIGatewayProxyResponseEvent that always adds CORS headers.
 */
public class ApiGatewayProxyResponseEventWithCors extends APIGatewayProxyResponseEvent {

  /**
   * Constructor.
   */
  public ApiGatewayProxyResponseEventWithCors() {
    setHeaders(new HashMap<>());
    getHeaders().put("Access-Control-Allow-Origin", "*");
    getHeaders().put("Access-Control-Allow-Headers", "*");
    getHeaders().put("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PATCH");
  }
}
