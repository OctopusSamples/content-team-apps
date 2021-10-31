package com.octopus.lambda;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the AWS Lambda proxy response.
 */
public class ProxyResponse {

  public final String statusCode;
  public final String body;
  public final Map<String, String> headers;

  /**
   * Constructor taking headers.
   *
   * @param statusCode The response HTTP status code.
   * @param body       The response body.
   * @param headers    The response headers.
   */
  public ProxyResponse(
      final String statusCode,
      final String body,
      final Map<String, String> headers) {
    this.headers = new HashMap<>();
    if (headers != null) {
      this.headers.putAll(headers);
    }
    this.body = body;
    this.statusCode = statusCode;
    addCorsHeaders();
  }

  /**
   * Constructor.
   *
   * @param statusCode The response HTTP status code.
   * @param body       The response body.
   */
  public ProxyResponse(
      final String statusCode,
      final String body) {
    this.headers = new HashMap<>();
    this.body = body;
    this.statusCode = statusCode;
    addCorsHeaders();
  }

  private void addCorsHeaders() {
    headers.put("Access-Control-Allow-Origin", "*");
  }
}