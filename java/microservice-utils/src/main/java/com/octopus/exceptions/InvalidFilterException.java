package com.octopus.exceptions;

/**
 * Represents an exception thrown a filter is not valid.
 */
public class InvalidFilterException extends RuntimeException {
  public InvalidFilterException() {
    super();
  }

  public InvalidFilterException(final Throwable ex) {
    super(ex);
  }

  public InvalidFilterException(final String message) {
    super(message);
  }
}
