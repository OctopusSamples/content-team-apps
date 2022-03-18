package com.octopus.exceptions;

/**
 * Represents an invalid attempt to pass a client generated Id.
 * https://jsonapi.org/format/#crud-creating-client-ids
 */
public class InvalidClientId extends RuntimeException {
  public InvalidClientId() {
    super();
  }
}
