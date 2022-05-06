package com.octopus.octopusproxy.application.http;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.google.common.net.HttpHeaders;
import com.octopus.Constants;
import com.octopus.jsonapi.AcceptHeaderVerifier;
import com.octopus.octopusproxy.application.Paths;
import com.octopus.octopusproxy.domain.handlers.ResourceHandler;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
public class JsonApiRootResourceGetAll {

  @Inject
  ResourceHandler resourceHandler;

  @Inject
  AcceptHeaderVerifier acceptHeaderVerifier;

  /**
   * The individual resource endpoint.
   *
   * @param filter       The RSQL filter.
   * @param acceptHeader The "Accept" headers.
   * @return An HTTP response object with the matching resource.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *                                        resource.
   */
  @GET
  @Produces(Constants.JsonApi.JSONAPI_CONTENT_TYPE)
  @Transactional
  public Response getOne(
      @QueryParam("filter") final String filter,
      @QueryParam("apiKey") final String apiKey,
      @HeaderParam(HttpHeaders.ACCEPT) final List<String> acceptHeader,
      @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorizationHeader,
      @HeaderParam(Constants.DATA_PARTITION_HEADER) final List<String> dataPartitionHeaders,
      @HeaderParam(Constants.SERVICE_AUTHORIZATION_HEADER) final String serviceAuthorizationHeader)
      throws DocumentSerializationException {
    acceptHeaderVerifier.checkAcceptHeader(acceptHeader);
    return Optional.ofNullable(resourceHandler.getAll(
            apiKey,
            filter,
            dataPartitionHeaders,
            authorizationHeader,
            serviceAuthorizationHeader))
        .map(d -> Response.ok(d).build())
        .orElse(Response.status(Status.NOT_FOUND).build());
  }
}
