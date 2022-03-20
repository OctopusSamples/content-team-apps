package com.octopus.loginmessage.domain.framework.providers;

import com.octopus.exceptions.EntityNotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.NonNull;

/**
 * Converts a EntityNotFound exception to a HTTP response.
 */
@Provider
public class EntityNotFoundMapper implements ExceptionMapper<EntityNotFoundException> {

  @Override
  public Response toResponse(@NonNull final EntityNotFoundException exception) {
    return Response.status(Status.NOT_FOUND.getStatusCode(), "The request resource was not found")
        .build();
  }
}
