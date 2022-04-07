package com.octopus.githubrepo.domain.framework.providers;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.NonNull;

/**
 * Converts a IllegalArgumentException exception to a HTTP response.
 */
@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

  @Override
  public Response toResponse(@NonNull final IllegalArgumentException exception) {
    return Response.status(Status.BAD_REQUEST.getStatusCode(), "The request was invalid")
        .build();
  }
}
