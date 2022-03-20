package com.octopus.exceptions;

/**
 * Represents an exception thrown when performing encryption.
 */
public class EncryptionException extends RuntimeException {
  public EncryptionException() {
    super();
  }

  public EncryptionException(final Throwable ex) {
    super(ex);
  }

  public EncryptionException(final String message) {
    super(message);
  }
}
