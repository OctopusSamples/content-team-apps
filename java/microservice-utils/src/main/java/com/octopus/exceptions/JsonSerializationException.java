package com.octopus.exceptions;

import lombok.NonNull;

/**
 * Represents an exception thrown when serializing objects to JSON.
 */
public class JsonSerializationException extends RuntimeException {
  public JsonSerializationException() {
    super();
  }

  public JsonSerializationException(@NonNull final Exception ex) {
    super(ex);
  }

  public JsonSerializationException(@NonNull final String message) {
    super(message);
  }
}
