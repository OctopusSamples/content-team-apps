package com.octopus.audits.application.http;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.audits.application.Constants;
import com.octopus.audits.domain.exceptions.InvalidAcceptHeaders;
import com.octopus.audits.domain.handlers.AuditsHandler;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
import lombok.NonNull;

/**
 * WHen this app is run as a web server, this class defines the REST API endpoints.
 */
@Path("/api/audits")
@RequestScoped
public class AuditResource {

  @Inject
  AuditsHandler auditsHandler;

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
  @Produces(Constants.JSONAPI_CONTENT_TYPE)
  @Transactional
  public Response getAll(
      @HeaderParam(Constants.DATA_PARTITION_HEADER) final List<String> dataPartitionHeaders,
      @HeaderParam(Constants.ACCEPT) final List<String> acceptHeader,
      @HeaderParam(Constants.AUTHORIZATION_HEADER) final List<String> authorizationHeader,
      @HeaderParam(Constants.SERVICE_AUTHORIZATION_HEADER) final List<String> serviceAuthorizationHeader,
      @QueryParam(Constants.FILTER_QUERY_PARAM) final String filter,
      @QueryParam(Constants.PAGE_OFFSET_QUERY_PARAM) final String pageOffset,
      @QueryParam(Constants.PAGE_LIMIT_QUERY_PARAM) final String pageLimit)
      throws DocumentSerializationException {
    checkAcceptHeader(acceptHeader);
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
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *                                        resource.
   */
  @POST
  @Consumes(Constants.JSONAPI_CONTENT_TYPE)
  @Produces(Constants.JSONAPI_CONTENT_TYPE)
  @Transactional
  public Response create(
      @NonNull final String document,
      @HeaderParam(Constants.ACCEPT) final List<String> acceptHeader,
      @HeaderParam(Constants.DATA_PARTITION_HEADER) final List<String> dataPartitionHeaders,
      @HeaderParam(Constants.AUTHORIZATION_HEADER) final List<String> authorizationHeader,
      @HeaderParam(Constants.SERVICE_AUTHORIZATION_HEADER) final List<String> serviceAuthorizationHeader)
      throws DocumentSerializationException {
    checkAcceptHeader(acceptHeader);
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
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *                                        resource.
   */
  @GET
  @Produces(Constants.JSONAPI_CONTENT_TYPE)
  @Path("{id}")
  @Transactional
  public Response getOne(
      @PathParam("id") final String id,
      @HeaderParam(Constants.ACCEPT) final List<String> acceptHeader,
      @HeaderParam(Constants.AUTHORIZATION_HEADER) final List<String> authorizationHeader,
      @HeaderParam(Constants.DATA_PARTITION_HEADER) final List<String> dataPartitionHeaders,
      @HeaderParam(Constants.SERVICE_AUTHORIZATION_HEADER) final List<String> serviceAuthorizationHeader)
      throws DocumentSerializationException {
    checkAcceptHeader(acceptHeader);
    return Optional.ofNullable(auditsHandler.getOne(id, dataPartitionHeaders,
            authorizationHeader.stream().findFirst().orElse(null),
            serviceAuthorizationHeader.stream().findFirst().orElse(null)))
        .map(d -> Response.ok(d).build())
        .orElse(Response.status(Status.NOT_FOUND).build());
  }

  private void checkAcceptHeader(final List<String> acceptHeader) {
    if (acceptHeader == null || acceptHeader.isEmpty()) {
      return;
    }

    final boolean allAcceptHeadersHaveMediaTypes =
        acceptHeader.stream()
            .filter(Objects::nonNull)
            .flatMap(h -> Arrays.stream(h.split(",")))
            .map(String::trim)
            .filter(h -> h.startsWith(Constants.JSONAPI_CONTENT_TYPE))
            .noneMatch(Constants.JSONAPI_CONTENT_TYPE::equals);

    if (allAcceptHeadersHaveMediaTypes) {
      throw new InvalidAcceptHeaders();
    }
  }
}
