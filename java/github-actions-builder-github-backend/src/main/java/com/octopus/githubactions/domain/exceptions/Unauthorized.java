package com.octopus.githubactions.domain.exceptions;

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
