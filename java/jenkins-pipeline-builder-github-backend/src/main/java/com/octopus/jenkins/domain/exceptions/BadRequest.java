package com.octopus.jenkins.domain.exceptions;

import lombok.NonNull;

/**
 * The exception thrown when the request was invalid.
 */
public class BadRequest extends RuntimeException {
  public BadRequest() {
    super();
  }
}
