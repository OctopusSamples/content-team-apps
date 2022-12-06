package com.octopus.products.application.http;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.Constants;
import com.octopus.products.application.Paths;
import com.octopus.products.domain.handlers.HealthHandler;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/** A resource to respond to health check requests. */
@Path(Paths.HEALTH_ENDPOINT)
@RequestScoped
public class HealthRootResourceCreate {

  @Inject HealthHandler healthHandler;

  /**
   * The health check.
   *
   * @return a HTTP response object.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *     resource.
   */
  @GET
  @Path("POST")
  @Produces({Constants.JsonApi.JSONAPI_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Transactional
  public Response healthCollectionPost() throws DocumentSerializationException {
    return Response
        .ok(healthHandler.getHealth(Paths.HEALTH_ENDPOINT, "POST"))
        .build();
  }
}
