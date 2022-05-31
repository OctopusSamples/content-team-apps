package com.octopus.githubrepo.application.http;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.google.common.net.HttpHeaders;
import com.octopus.Constants;
import com.octopus.githubrepo.GlobalConstants;
import com.octopus.githubrepo.domain.ServiceConstants;
import com.octopus.githubrepo.domain.handlers.GitHubCommitHandler;
import com.octopus.githubrepo.domain.handlers.GitHubRepoHandler;
import com.octopus.jsonapi.AcceptHeaderVerifier;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.gradle.internal.service.scopes.Scope.Global;

/** WHen this app is run as a web server, this class defines the REST API endpoints. */
@Path("/api")
@RequestScoped
public class GitHubCommitResource {

  @Inject
  GitHubCommitHandler gitHubCommitHandler;

  @Inject AcceptHeaderVerifier acceptHeaderVerifier;

  /**
   * The resource creation endpoint.
   *
   * @param document The JSONAPI resource to create.
   * @param acceptHeader The "Accept" headers.
   * @param githubToken The GitHub OAuth access token.
   * @return An HTTP response object with the created resource.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *     resource.
   */
  @POST
  @Path("/githubcommit")
  @Consumes(Constants.JsonApi.JSONAPI_CONTENT_TYPE)
  @Produces(Constants.JsonApi.JSONAPI_CONTENT_TYPE)
  @Transactional
  public Response create(
      final String document,
      @HeaderParam(HttpHeaders.ACCEPT) final List<String> acceptHeader,
      @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorizationHeader,
      @HeaderParam(GlobalConstants.DATA_PARTITION) final String dataPartition,
      @HeaderParam(GlobalConstants.AMAZON_TRACE_ID_HEADER) final String xray,
      @HeaderParam(Constants.SERVICE_AUTHORIZATION_HEADER) final String serviceAuthorizationHeader,
      @HeaderParam(Constants.ROUTING_HEADER) final String routingHeader,
      @CookieParam(ServiceConstants.GITHUB_SESSION_COOKIE) final String githubToken)
      throws DocumentSerializationException {
    acceptHeaderVerifier.checkAcceptHeader(acceptHeader);
    return Response.accepted(
            gitHubCommitHandler.create(
                document,
                authorizationHeader,
                serviceAuthorizationHeader,
                routingHeader,
                dataPartition,
                xray,
                githubToken))
        .build();
  }
}
