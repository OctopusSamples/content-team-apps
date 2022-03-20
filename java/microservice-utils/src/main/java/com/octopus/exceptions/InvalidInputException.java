package com.octopus.exceptions;

/**
 * The exception thrown when a new or updated entity fails validation.
 */
public class InvalidInputException extends RuntimeException {

  public InvalidInputException() {
    super();
  }

  public InvalidInputException(final Throwable cause) {
    super(cause);
  }

  public InvalidInputException(final String message) {
    super(message);
  }
}
