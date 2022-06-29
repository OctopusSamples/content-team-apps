package com.octopus.githubrepo.application.http;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.githubrepo.domain.handlers.HealthHandler;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/** A resource to respond to health check requests. */
@Path("/health/populategithubrepo")
@RequestScoped
public class HealthResource {

  @Inject HealthHandler healthHandler;

  /**
   * The health check.
   *
   * @return a HTTP response object.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *     resource.
   */
  @GET
  @Path("GET")
  public Uni<Response> healthCollectionGet() throws DocumentSerializationException {
    return Uni.createFrom().item(
        Response.ok(healthHandler.getHealth("/health/populategithubrepo", "GET")).build());
  }

  /**
   * The health check.
   *
   * @return a HTTP response object.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *     resource.
   */
  @GET
  @Path("POST")
  public Uni<Response> healthCollectionPost() throws DocumentSerializationException {
    return Uni.createFrom().item(
        Response.ok(healthHandler.getHealth("/health/populategithubrepo", "POST")).build());
  }

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
    return Uni.createFrom().item(
        Response.ok(healthHandler.getHealth("/health/customers/" + id, "GET")).build());
  }
}
