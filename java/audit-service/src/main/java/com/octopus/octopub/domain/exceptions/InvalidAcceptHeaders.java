package com.octopus.octopub.domain.exceptions;

import lombok.NonNull;

/**
 * The exception thrown when a request includes accept headers contain the JSON API content type,
 * and also all include media type parameters. From the spec
 * https://jsonapi.org/format/#content-negotiation-servers:
 *
 * <p>Servers MUST respond with a 406 Not Acceptable status code if a request’s Accept header
 * contains the JSON:API media type and all instances of that media type are modified with media
 * type parameters.
 */
public class InvalidAcceptHeaders extends RuntimeException {
  public InvalidAcceptHeaders() {
    super();
  }

  public InvalidAcceptHeaders(@NonNull final Exception ex) {
    super(ex);
  }
}
