package com.octopus.githubactions.domain.framework.providers;

import com.octopus.githubactions.domain.exceptions.ServerError;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.NonNull;

/**
 * Converts a ServerError exception to a HTTP response.
 */
@Provider
public class ServerErrorMapper implements ExceptionMapper<ServerError> {

  @Override
  public Response toResponse(@NonNull final ServerError exception) {
    return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode(), "The server encountered an internal error")
        .build();
  }
}
