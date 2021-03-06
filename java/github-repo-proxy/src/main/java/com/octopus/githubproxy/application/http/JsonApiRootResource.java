package com.octopus.githubproxy.application.http;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.google.common.net.HttpHeaders;
import com.octopus.Constants;
import com.octopus.githubproxy.ServiceConstants;
import com.octopus.githubproxy.application.Paths;
import com.octopus.githubproxy.domain.handlers.ResourceHandler;
import com.octopus.jsonapi.AcceptHeaderVerifier;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * WHen this app is run as a web server, this class defines the REST API endpoints.
 */
@Path(Paths.API_ENDPOINT)
@RequestScoped
public class JsonApiRootResource {

  @Inject
  ResourceHandler resourceHandler;

  @Inject
  AcceptHeaderVerifier acceptHeaderVerifier;

  /**
   * The individual resource endpoint.
   *
   * @param id           The ID of the resource to return.
   * @param acceptHeader The "Accept" headers.
   * @return An HTTP response object with the matching resource.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *                                        resource.
   */
  @GET
  @Produces(Constants.JsonApi.JSONAPI_CONTENT_TYPE)
  @Path("{id}")
  public Uni<Response> getOne(
      @PathParam("id") final String id,
      @HeaderParam(HttpHeaders.ACCEPT) final List<String> acceptHeader,
      @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorizationHeader,
      @HeaderParam(Constants.DATA_PARTITION_HEADER) final List<String> dataPartitionHeaders,
      @HeaderParam(Constants.SERVICE_AUTHORIZATION_HEADER) final String serviceAuthorizationHeader,
      @CookieParam(ServiceConstants.GITHUB_SESSION_COOKIE) final String githubToken)
      throws DocumentSerializationException {
    acceptHeaderVerifier.checkAcceptHeader(acceptHeader);
    return resourceHandler.getOne(
            id,
            dataPartitionHeaders,
            authorizationHeader,
            serviceAuthorizationHeader,
            githubToken)
        .map(r -> Response.ok(r).build());
  }
}
