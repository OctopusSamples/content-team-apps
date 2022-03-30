package com.octopus.githubactions.github.domain.exceptions;

import lombok.NonNull;

/**
 * The exception thrown when the request was invalid.
 */
public class BadRequest extends RuntimeException {
  public BadRequest() {
    super();
  }

  public BadRequest(@NonNull final Exception ex) {
    super(ex);
  }
}
