package com.octopus.octopusproxy.domain.framework.providers;

import com.octopus.exceptions.JsonSerializationException;
import com.octopus.features.MicroserviceNameFeature;
import io.quarkus.logging.Log;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.NonNull;

/**
 * Converts a JsonSerializationException to an HTTP response.
 */
@Provider
public class JsonSerializationExceptionMapper
    implements ExceptionMapper<JsonSerializationException> {

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  @Override
  public Response toResponse(@NonNull final JsonSerializationException exception) {
    Log.error(microserviceNameFeature.getMicroserviceName() + "-Serialization-SerializationFailed", exception);

    return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode(), exception.toString())
        .build();
  }
}
