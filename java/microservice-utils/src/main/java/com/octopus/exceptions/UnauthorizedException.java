package com.octopus.exceptions;

/**
 * The exception thrown when the request is unauthorized.
 */
public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException() {
    super();
  }

  public UnauthorizedException(final Throwable cause) {
    super(cause);
  }

  public UnauthorizedException(final String message) {
    super(message);
  }

  public UnauthorizedException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
