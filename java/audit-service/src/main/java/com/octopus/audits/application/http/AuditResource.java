package com.octopus.audits.application.http;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.audits.GlobalConstants;
import com.octopus.audits.domain.handlers.AuditsHandler;
import com.octopus.audits.domain.jsonapi.AcceptHeaderVerifier;
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
@Path("/api/audits")
@RequestScoped
public class AuditResource {

  @Inject
  AuditsHandler auditsHandler;

  @Inject
  AcceptHeaderVerifier acceptHeaderVerifier;

  /**
   * The resource collection endpoint.
   *
   * @param acceptHeader The "Accept" headers.
   * @param filter       The RSQL query string.
   * @return a HTTP response object.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI resource.
   */
  @GET
  @Produces(GlobalConstants.JSONAPI_CONTENT_TYPE)
  @Transactional
  public Response getAll(
      @HeaderParam(GlobalConstants.DATA_PARTITION_HEADER) final List<String> dataPartitionHeaders,
      @HeaderParam(GlobalConstants.ACCEPT) final List<String> acceptHeader,
      @HeaderParam(GlobalConstants.AUTHORIZATION_HEADER) final List<String> authorizationHeader,
      @HeaderParam(GlobalConstants.SERVICE_AUTHORIZATION_HEADER) final List<String> serviceAuthorizationHeader,
      @QueryParam(GlobalConstants.FILTER_QUERY_PARAM) final String filter,
      @QueryParam(GlobalConstants.PAGE_OFFSET_QUERY_PARAM) final String pageOffset,
      @QueryParam(GlobalConstants.PAGE_LIMIT_QUERY_PARAM) final String pageLimit)
      throws DocumentSerializationException {
    acceptHeaderVerifier.checkAcceptHeader(acceptHeader);
    return Response.ok(auditsHandler.getAll(
            dataPartitionHeaders,
            filter,
            pageOffset,
            pageLimit,
            authorizationHeader.stream().findFirst().orElse(null),
            serviceAuthorizationHeader.stream().findFirst().orElse(null)))
        .build();
  }

  /**
   * The resource creation endpoint.
   *
   * @param document     The JSONAPI resource to create.
   * @param acceptHeader The "Accept" headers.
   * @return An HTTP response object with the created resource.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI resource.
   */
  @POST
  @Consumes(GlobalConstants.JSONAPI_CONTENT_TYPE)
  @Produces(GlobalConstants.JSONAPI_CONTENT_TYPE)
  @Transactional
  public Response create(
      final String document,
      @HeaderParam(GlobalConstants.ACCEPT) final List<String> acceptHeader,
      @HeaderParam(GlobalConstants.DATA_PARTITION_HEADER) final List<String> dataPartitionHeaders,
      @HeaderParam(GlobalConstants.AUTHORIZATION_HEADER) final List<String> authorizationHeader,
      @HeaderParam(GlobalConstants.SERVICE_AUTHORIZATION_HEADER) final List<String> serviceAuthorizationHeader)
      throws DocumentSerializationException {
    acceptHeaderVerifier.checkAcceptHeader(acceptHeader);
    return Response.ok(auditsHandler.create(
            document,
            dataPartitionHeaders,
            authorizationHeader.stream().findFirst().orElse(null),
            serviceAuthorizationHeader.stream().findFirst().orElse(null)))
        .build();
  }

  /**
   * The individual resource endpoint.
   *
   * @param id           The ID of the resource to return.
   * @param acceptHeader The "Accept" headers.
   * @return An HTTP response object with the matching resource.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI resource.
   */
  @GET
  @Produces(GlobalConstants.JSONAPI_CONTENT_TYPE)
  @Path("{id}")
  @Transactional
  public Response getOne(
      @PathParam("id") final String id,
      @HeaderParam(GlobalConstants.ACCEPT) final List<String> acceptHeader,
      @HeaderParam(GlobalConstants.AUTHORIZATION_HEADER) final List<String> authorizationHeader,
      @HeaderParam(GlobalConstants.DATA_PARTITION_HEADER) final List<String> dataPartitionHeaders,
      @HeaderParam(GlobalConstants.SERVICE_AUTHORIZATION_HEADER) final List<String> serviceAuthorizationHeader)
      throws DocumentSerializationException {
    acceptHeaderVerifier.checkAcceptHeader(acceptHeader);
    final String response = auditsHandler.getOne(id, dataPartitionHeaders,
        authorizationHeader.stream().findFirst().orElse(null),
        serviceAuthorizationHeader.stream().findFirst().orElse(null));
    return Response.ok(response).build();
  }
}
