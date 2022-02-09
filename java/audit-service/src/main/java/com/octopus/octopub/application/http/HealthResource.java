package com.octopus.octopub.application.http;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.octopub.domain.handlers.HealthHandler;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/** A resource to respond to health check requests. */
@Path("/health/audits")
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
  @GET()
  @Path("GET")
  @Transactional
  public Response healthCollectionGet() throws DocumentSerializationException {
    return Response.ok(healthHandler.getHealth("/health/audits", "GET")).build();
  }

  /**
   * The health check.
   *
   * @return a HTTP response object.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *     resource.
   */
  @GET()
  @Path("POST")
  @Transactional
  public Response healthCollectionPost() throws DocumentSerializationException {
    return Response.ok(healthHandler.getHealth("/health/audits", "POST")).build();
  }

  /**
   * The health check.
   *
   * @return a HTTP response object.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *     resource.
   */
  @GET()
  @Path("{id}/GET")
  @Transactional
  public Response healthIndividualGet(@PathParam("id") final String id) throws DocumentSerializationException {
    return Response.ok(healthHandler.getHealth("/health/audits/" + id, "GET")).build();
  }
}
