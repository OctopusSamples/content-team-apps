package com.octopus.exceptions;

/**
 * The exception thrown when the server experiences a general failure.
 */
public class ServerErrorException extends RuntimeException {

  public ServerErrorException() {
    super();
  }

  public ServerErrorException(final Throwable cause) {
    super(cause);
  }

  public ServerErrorException(final String message) {
    super(message);
  }
}
