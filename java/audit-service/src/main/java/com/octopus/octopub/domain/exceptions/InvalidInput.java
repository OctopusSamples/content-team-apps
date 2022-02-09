package com.octopus.octopub.domain.exceptions;

import lombok.NonNull;

/** The exception thrown when a new or updated entity fails validation. */
public class InvalidInput extends RuntimeException {
  public InvalidInput() {
    super();
  }

  public InvalidInput(@NonNull final Exception ex) {
    super(ex);
  }

  public InvalidInput(@NonNull final String message) {
    super(message);
  }
}
