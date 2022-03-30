package com.octopus.githubactions.github.domain.framework.providers;

import com.octopus.githubactions.github.domain.exceptions.BadRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.NonNull;

/**
 * Converts a BadRequest exception to a HTTP response.
 */
@Provider
public class BadRequestMapper implements ExceptionMapper<BadRequest> {

  @Override
  public Response toResponse(@NonNull final BadRequest exception) {
    return Response.status(Status.BAD_REQUEST.getStatusCode(), "The request was not valid")
        .build();
  }
}
