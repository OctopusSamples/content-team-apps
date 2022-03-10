package com.octopus.githuboauth.domain.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * A simplified HTTP response.
 */
@Data
public class SimpleResponse {

  private Integer code;
  private String body;
  private Map<String, String> headers;
  private Map<String, List<String>> multiValueHeaders;

  public SimpleResponse() {
  }

  public SimpleResponse(final int code, final String body) {
    this(code, body, new HashMap<>());
  }

  public SimpleResponse(final int code, final Map<String, String> headers) {
    this(code, "", headers);
  }

  public SimpleResponse(final int code, final Map<String, String> headers,
      final Map<String, List<String>> multiValueHeaders) {
    this(code, "", headers, multiValueHeaders);
  }

  public SimpleResponse(final int code, final String body, final Map<String, String> headers) {
    this(code, body, headers, new HashMap<>());
  }

  /**
   * Constructor taking HTTP response code, body, headers, and multivalue headers.
   *
   * @param code              The HTTP response code.
   * @param body              The HTTP response body.
   * @param headers           The HTTP headers.
   * @param multiValueHeaders The multivalue HTTP headers.
   */
  public SimpleResponse(final int code, final String body, final Map<String, String> headers,
      final Map<String, List<String>> multiValueHeaders) {
    this.code = code;
    this.body = body;
    this.headers = headers;
    this.multiValueHeaders = multiValueHeaders;
  }
}
