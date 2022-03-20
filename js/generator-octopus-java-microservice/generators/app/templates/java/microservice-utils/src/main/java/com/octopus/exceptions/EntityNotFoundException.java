package com.octopus.exceptions;

/**
 * The exception thrown when a requested entity can not be found (or will not be found due to
 * security or data partitioning rules).
 */
public class EntityNotFoundException extends RuntimeException {
  public EntityNotFoundException() {
    super();
  }

  public EntityNotFoundException(final Throwable cause) {
    super(cause);
  }

  public EntityNotFoundException(final String message) {
    super(message);
  }

  public EntityNotFoundException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
