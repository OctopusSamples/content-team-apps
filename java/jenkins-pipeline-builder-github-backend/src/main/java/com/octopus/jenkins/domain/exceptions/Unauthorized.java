package com.octopus.jenkins.domain.exceptions;

import lombok.NonNull;

/**
 * The exception thrown when the request is unauthorized.
 */
public class Unauthorized extends RuntimeException {
  public Unauthorized() {
    super();
  }

  public Unauthorized(@NonNull final Exception ex) {
    super(ex);
  }
}
