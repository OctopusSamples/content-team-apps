package com.octopus.exceptions;

/**
 * Represents an invalid attempt to pass a client generated Id.
 * https://jsonapi.org/format/#crud-creating-client-ids
 */
public class InvalidClientIdException extends RuntimeException {
  public InvalidClientIdException() {
    super();
  }

  public InvalidClientIdException(final Throwable cause) {
    super(cause);
  }

  public InvalidClientIdException(final String message) {
    super(message);
  }

  public InvalidClientIdException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
