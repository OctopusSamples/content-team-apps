package com.octopus.githubactions.domain.exceptions;

import lombok.NonNull;

/**
 * The exception thrown when a requested entity can not be found (or will not be found due to
 * security or data partitioning rules).
 */
public class ServerError extends RuntimeException {
  public ServerError() {
    super();
  }

  public ServerError(@NonNull final Exception ex) {
    super(ex);
  }
}
