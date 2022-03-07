package com.octopus.githubrepo.application.http;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.google.common.net.HttpHeaders;
import com.octopus.Constants;
import com.octopus.githubrepo.domain.handlers.GitHubRepoHandler;
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

/** WHen this app is run as a web server, this class defines the REST API endpoints. */
@Path("/api/githubrepo")
@RequestScoped
public class ServiceAccountResource {

  @Inject
  GitHubRepoHandler gitHubRepoHandler;

  @Inject AcceptHeaderVerifier acceptHeaderVerifier;

  /**
   * The resource creation endpoint.
   *
   * @param document The JSONAPI resource to create.
   * @param acceptHeader The "Accept" headers.
   * @param idToken The ID token from Octofront.
   * @return An HTTP response object with the created resource.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *     resource.
   */
  @POST
  @Consumes(Constants.JsonApi.JSONAPI_CONTENT_TYPE)
  @Produces(Constants.JsonApi.JSONAPI_CONTENT_TYPE)
  @Transactional
  public Response create(
      final String document,
      @HeaderParam(HttpHeaders.ACCEPT) final List<String> acceptHeader,
      @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorizationHeader,
      @HeaderParam(Constants.SERVICE_AUTHORIZATION_HEADER) final String serviceAuthorizationHeader)
      throws DocumentSerializationException {
    acceptHeaderVerifier.checkAcceptHeader(acceptHeader);
    return Response.ok(
            gitHubRepoHandler.create(
                document, authorizationHeader, serviceAuthorizationHeader))
        .build();
  }
}
