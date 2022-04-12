package com.octopus.githubrepo.domain.framework.providers;

import com.octopus.exceptions.ServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.NonNull;

/**
 * Maps ServerErrorException to a HTTP response.
 */
@Provider
public class ServerErrorExceptionMapper implements ExceptionMapper<ServerErrorException> {

  @Override
  public Response toResponse(@NonNull final ServerErrorException exception) {

    return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode(), exception.toString())
        .build();
  }
}
