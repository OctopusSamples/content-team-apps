package com.octopus.octopub.domain.framework.providers;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.octopub.GlobalConstants;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * Converts a DocumentSerializationException to a HTTP response.
 */
@Provider
public class DocumentSerializationExceptionMapper
    implements ExceptionMapper<DocumentSerializationException> {

  @Inject
  Logger log;

  @Override
  public Response toResponse(@NonNull final DocumentSerializationException exception) {
    log.error(GlobalConstants.MICROSERVICE_NAME + "-Serialization-SerializationFailed", exception);

    return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode(), exception.toString())
        .build();
  }
}
