package com.octopus.loginmessage.application.http;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.google.common.net.HttpHeaders;
import com.octopus.Constants;
import com.octopus.loginmessage.application.Paths;
import com.octopus.loginmessage.domain.handlers.ResourceHandler;
import com.octopus.jsonapi.AcceptHeaderVerifier;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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
      @HeaderParam(Constants.SERVICE_AUTHORIZATION_HEADER) final String serviceAuthorizationHeader,
      @HeaderParam(Constants.AMAZON_TRACE_ID_HEADER) final String xray)
      throws DocumentSerializationException {
    acceptHeaderVerifier.checkAcceptHeader(acceptHeader);
    resourceHandler.create(
        document,
        dataPartitionHeaders,
        authorizationHeader,
        serviceAuthorizationHeader,
        xray);
    return Response.status(202).build();
  }

}
