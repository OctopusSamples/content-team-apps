package com.octopus.products.domain.framework.providers;

import com.github.jasminb.jsonapi.exceptions.InvalidJsonApiResourceException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.NonNull;

/**
 * Converts a InvalidJsonApiResourceException exception to a HTTP response.
 */
@Provider
public class InvalidJsonApiResourceExceptionMapper
    implements ExceptionMapper<InvalidJsonApiResourceException> {

  @Override
  public Response toResponse(@NonNull final InvalidJsonApiResourceException exception) {
    return Response.status(Status.BAD_REQUEST.getStatusCode(), exception.toString()).build();
  }
}
