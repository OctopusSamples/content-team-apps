package com.octopus.customers.domain.framework.providers;

import com.octopus.exceptions.InvalidAcceptHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.NonNull;

/**
 * Converts a InvalidAcceptHeaders exception to a HTTP response.
 */
@Provider
public class InvalidAcceptHeadersMapper implements ExceptionMapper<InvalidAcceptHeaders> {

  @Override
  public Response toResponse(@NonNull final InvalidAcceptHeaders exception) {
    return Response.status(Status.NOT_ACCEPTABLE.getStatusCode()).build();
  }
}
