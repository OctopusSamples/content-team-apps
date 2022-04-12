package com.octopus.githubrepo.domain.exceptions;

/**
 * Represents an exception thrown when an upstream api call returned an iunexpected response.
 */
public class UnexpectedResponseException extends RuntimeException {
  public UnexpectedResponseException() {
    super();
  }

  public UnexpectedResponseException(final Throwable ex) {
    super(ex);
  }

  public UnexpectedResponseException(final String message) {
    super(message);
  }
}
