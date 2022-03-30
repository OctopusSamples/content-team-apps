package com.octopus.githubactions.github.domain.framework.providers;

import com.octopus.githubactions.github.domain.exceptions.Unauthorized;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.NonNull;

/**
 * Converts a Unauthorized exception to a HTTP response.
 */
@Provider
public class UnauthorizedMapper implements ExceptionMapper<Unauthorized> {

  @Override
  public Response toResponse(@NonNull final Unauthorized exception) {
    return Response.status(Status.UNAUTHORIZED.getStatusCode(), "The request resource could not be accessed")
        .build();
  }
}
