package com.octopus.products.domain.framework.providers;

import cz.jirutka.rsql.parser.RSQLParserException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.NonNull;

/**
 * Converts a RSQLParserException exception to a HTTP response.
 */
@Provider
public class RsqlParserExceptionMapper implements ExceptionMapper<RSQLParserException> {

  @Override
  public Response toResponse(@NonNull final RSQLParserException exception) {
    return Response.status(Status.BAD_REQUEST.getStatusCode(), "The supplied filter was invalid")
        .build();
  }
}
