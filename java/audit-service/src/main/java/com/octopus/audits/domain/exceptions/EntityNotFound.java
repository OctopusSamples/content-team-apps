package com.octopus.audits.domain.exceptions;

/**
 * The exception thrown when a requested entity can not be found (or will not be found due to
 * security or data partitioning rules).
 */
public class EntityNotFound extends RuntimeException {
  public EntityNotFound() {
    super();
  }
}
