package com.octopus.githubrepo.domain.framework.providers;

import com.octopus.exceptions.InvalidInputException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.NonNull;

/**
 * Converts a InvalidInput exception to a HTTP response.
 */
@Provider
public class InvalidInputExceptionMapper implements ExceptionMapper<InvalidInputException> {

  @Override
  public Response toResponse(@NonNull final InvalidInputException exception) {
    return Response.status(Status.BAD_REQUEST.getStatusCode(), exception.toString()).build();
  }
}
