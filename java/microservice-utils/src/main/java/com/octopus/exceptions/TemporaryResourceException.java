package com.octopus.exceptions;

import lombok.NonNull;

/**
 * The exception thrown when a temporary resource could not be cleaned up.
 */
public class TemporaryResourceException extends RuntimeException {
  public TemporaryResourceException() {
    super();
  }

  public TemporaryResourceException(@NonNull final Throwable cause) {
    super(cause);
  }

  public TemporaryResourceException(@NonNull final String message) {
    super(message);
  }

  public TemporaryResourceException(@NonNull final String message, @NonNull final Throwable cause) {
    super(message, cause);
  }
}
