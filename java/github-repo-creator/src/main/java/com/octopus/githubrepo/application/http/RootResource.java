package com.octopus.githubrepo.application.http;

import io.smallrye.mutiny.Uni;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/** A resource to respond to health check requests. */
@Path("/")
@RequestScoped
public class RootResource {

  /**
   * Platforms like the AWS ALB Ingress controller expect services to respond on the root path
   * for health checks.
   *
   * @return a HTTP response object.
   */
  @GET
  public Uni<Response> healthCollectionGet()  {
    return Uni.createFrom().item(Response.ok().build());
  }
}
