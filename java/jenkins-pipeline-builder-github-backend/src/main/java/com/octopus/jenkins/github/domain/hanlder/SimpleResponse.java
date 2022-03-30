package com.octopus.jenkins.github.domain.hanlder;

import lombok.Data;

/**
 * A simplified HTTP response.
 */
@Data
public class SimpleResponse {
  private int code;
  private String body;

  public SimpleResponse() {}

  public SimpleResponse(final int code, final String body) {
    this.code = code;
    this.body = body;
  }
}
