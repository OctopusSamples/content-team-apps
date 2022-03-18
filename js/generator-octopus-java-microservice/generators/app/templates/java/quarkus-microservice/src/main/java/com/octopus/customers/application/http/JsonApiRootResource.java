package com.octopus.customers.application.http;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.google.common.net.HttpHeaders;
import com.octopus.Constants;
import com.octopus.customers.application.Paths;
import com.octopus.customers.domain.handlers.ResourceHandler;
import com.octopus.jsonapi.AcceptHeaderVerifier;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
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
   * The resource collection endpoint.
   *
   * @param acceptHeader The "Accept" headers.
   * @param filter       The RSQL query string.
   * @return a HTTP response object.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *                                        resource.
   */
  @GET
  @Produces(Constants.JsonApi.JSONAPI_CONTENT_TYPE)
  @Transactional
  public Response getAll(
      @HeaderParam(Constants.DATA_PARTITION_HEADER) final List<String> dataPartitionHeaders,
      @HeaderParam(HttpHeaders.ACCEPT) final List<String> acceptHeader,
      @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorizationHeader,
      @HeaderParam(Constants.SERVICE_AUTHORIZATION_HEADER) final String serviceAuthorizationHeader,
      @QueryParam(Constants.JsonApi.FILTER_QUERY_PARAM) final String filter,
      @QueryParam(Constants.JsonApi.PAGE_OFFSET_QUERY_PARAM) final String pageOffset,
      @QueryParam(Constants.JsonApi.PAGE_LIMIT_QUERY_PARAM) final String pageLimit)
      throws DocumentSerializationException {
    acceptHeaderVerifier.checkAcceptHeader(acceptHeader);
    return Response.ok(resourceHandler.getAll(
            dataPartitionHeaders,
            filter,
            pageOffset,
            pageLimit,
            authorizationHeader,
            serviceAuthorizationHeader))
        .build();
  }

  /**
   * The resource creation endpoint.
   *
   * @param document     The JSONAPI resource to create.
   * @param acceptHeader The "Accept" headers.
   * @return An HTTP response object with the created resource.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *                                        resource.
   */
  @POST
  @Consumes(Constants.JsonApi.JSONAPI_CONTENT_TYPE)
  @Produces(Constants.JsonApi.JSONAPI_CONTENT_TYPE)
  @Transactional
  public Response create(
      final String document,
      @HeaderParam(HttpHeaders.ACCEPT) final List<String> acceptHeader,
      @HeaderParam(Constants.DATA_PARTITION_HEADER) final List<String> dataPartitionHeaders,
      @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorizationHeader,
      @HeaderParam(Constants.SERVICE_AUTHORIZATION_HEADER) final String serviceAuthorizationHeader)
      throws DocumentSerializationException {
    acceptHeaderVerifier.checkAcceptHeader(acceptHeader);
    return Response.ok(resourceHandler.create(
            document,
            dataPartitionHeaders,
            authorizationHeader,
            serviceAuthorizationHeader))
        .build();
  }

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
  @Transactional
  public Response getOne(
      @PathParam("id") final String id,
      @HeaderParam(HttpHeaders.ACCEPT) final List<String> acceptHeader,
      @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorizationHeader,
      @HeaderParam(Constants.DATA_PARTITION_HEADER) final List<String> dataPartitionHeaders,
      @HeaderParam(Constants.SERVICE_AUTHORIZATION_HEADER) final String serviceAuthorizationHeader)
      throws DocumentSerializationException {
    acceptHeaderVerifier.checkAcceptHeader(acceptHeader);
    return Optional.ofNullable(resourceHandler.getOne(
            id,
            dataPartitionHeaders,
            authorizationHeader,
            serviceAuthorizationHeader))
        .map(d -> Response.ok(d).build())
        .orElse(Response.status(Status.NOT_FOUND).build());
  }
}
