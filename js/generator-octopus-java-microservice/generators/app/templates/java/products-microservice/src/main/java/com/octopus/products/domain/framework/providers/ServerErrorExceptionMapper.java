package com.octopus.products.domain.framework.providers;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.NonNull;

/**
 * Converts a EntityNotFoundException exception to a HTTP response.
 */
@Provider
public class ServerErrorExceptionMapper implements ExceptionMapper<ServerErrorException> {

  @Override
  public Response toResponse(@NonNull final ServerErrorException exception) {
    return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode(), "A server error was encountered.")
        .build();
  }
}
