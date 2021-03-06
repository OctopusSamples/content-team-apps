package com.octopus.githubproxy.application.http;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.githubproxy.application.Paths;
import com.octopus.githubproxy.domain.handlers.HealthHandler;
import io.smallrye.mutiny.Uni;
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
public class HealthRootResource {

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
  public Uni<Response> healthIndividualGet(@PathParam("id") final String id) throws DocumentSerializationException {
    return Uni.createFrom().item(Response
        .ok(healthHandler.getHealth(Paths.HEALTH_ENDPOINT + id, "GET"))
        .build());
  }
}
