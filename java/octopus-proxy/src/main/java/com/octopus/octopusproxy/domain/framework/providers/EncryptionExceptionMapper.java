package com.octopus.octopusproxy.domain.framework.providers;

import com.octopus.exceptions.EncryptionException;
import com.octopus.exceptions.EntityNotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.NonNull;

/**
 * Converts a EntityNotFoundException exception to a HTTP response.
 */
@Provider
public class EncryptionExceptionMapper implements ExceptionMapper<EncryptionException> {

  @Override
  public Response toResponse(@NonNull final EncryptionException exception) {
    return Response.status(Status.BAD_REQUEST.getStatusCode(), "An encrypted value was invalid")
        .build();
  }
}
