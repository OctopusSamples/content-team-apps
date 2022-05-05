package com.octopus.octopusproxy.application.http;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.octopusproxy.application.Paths;
import com.octopus.octopusproxy.domain.handlers.HealthHandler;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/** A resource to respond to health check requests. */
@Path(Paths.HEALTH_ENDPOINT)
@RequestScoped
public class HealthRootResourceGetOne {

  @Inject HealthHandler healthHandler;

  /**
   * The health check.
   *
   * @return a HTTP response object.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *     resource.
   */
  @GET
  @Path("{id}/GET")
  @Transactional
  public Response healthIndividualGet(@PathParam("id") final String id) throws DocumentSerializationException {
    return Response
        .ok(healthHandler.getHealth(Paths.HEALTH_ENDPOINT + id, "GET"))
        .build();
  }
}