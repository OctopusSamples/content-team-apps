package com.octopus.exceptions;

/**
 * The exception thrown when a temporary resource could not be cleaned up.
 */
public class TemporaryResourceException extends RuntimeException {
  public TemporaryResourceException() {
    super();
  }

  public TemporaryResourceException(final Throwable cause) {
    super(cause);
  }

  public TemporaryResourceException(final String message) {
    super(message);
  }

  public TemporaryResourceException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
