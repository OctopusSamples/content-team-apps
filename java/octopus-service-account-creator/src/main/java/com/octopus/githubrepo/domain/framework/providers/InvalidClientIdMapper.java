package com.octopus.githubrepo.domain.framework.providers;

import com.octopus.exceptions.InvalidClientIdException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.NonNull;

/**
 * Converts a InvalidClientId exception to a forbidden HTTP response.
 * See https://jsonapi.org/format/#crud-creating-client-ids.
 */
@Provider
public class InvalidClientIdMapper implements ExceptionMapper<InvalidClientIdException> {

  @Override
  public Response toResponse(@NonNull final InvalidClientIdException exception) {
    return Response.status(Status.FORBIDDEN.getStatusCode(), "The request resource was not found")
        .build();
  }
}
