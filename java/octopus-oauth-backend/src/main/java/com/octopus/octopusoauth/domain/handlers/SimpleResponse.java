package com.octopus.octopusoauth.domain.handlers;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * A simplified HTTP response.
 */
@Data
public class SimpleResponse {
  private int code;
  private String body;
  private Map<String, String> headers;

  public SimpleResponse() {}

  public SimpleResponse(final int code, final String body) {
    this(code, body, new HashMap<>());
  }

  public SimpleResponse(final int code, final Map<String, String> headers) {
    this(code, "", headers);
  }

  public SimpleResponse(final int code, final String body, final Map<String, String> headers) {
    this.code = code;
    this.body = body;
    this.headers = headers;
  }
}
