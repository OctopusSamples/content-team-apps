package com.octopus.exceptions;

import lombok.NonNull;

/**
 * Represents an exception thrown when performing encryption.
 */
public class EncryptionException extends RuntimeException {
  public EncryptionException() {
    super();
  }

  public EncryptionException(@NonNull final Exception ex) {
    super(ex);
  }

  public EncryptionException(@NonNull final String message) {
    super(message);
  }
}
