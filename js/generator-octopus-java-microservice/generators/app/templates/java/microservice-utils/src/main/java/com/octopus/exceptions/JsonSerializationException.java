package com.octopus.exceptions;

/**
 * Represents an exception thrown when serializing objects to JSON.
 */
public class JsonSerializationException extends RuntimeException {
  public JsonSerializationException() {
    super();
  }

  public JsonSerializationException(final Throwable ex) {
    super(ex);
  }

  public JsonSerializationException(final String message) {
    super(message);
  }
}
