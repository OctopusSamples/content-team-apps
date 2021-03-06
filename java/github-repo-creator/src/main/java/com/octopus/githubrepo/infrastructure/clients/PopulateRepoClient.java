package com.octopus.githubrepo.infrastructure.clients;

import com.octopus.Constants.JsonApi;
import com.octopus.githubrepo.GlobalConstants;
import com.octopus.githubrepo.domain.ServiceConstants;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.concurrent.CompletionStage;

/**
  A REST client accessing the repo population service.
 */
@Path("/api")
@RegisterRestClient
public interface PopulateRepoClient {
  @POST
  @Path("populategithubrepo")
  @Produces(JsonApi.JSONAPI_CONTENT_TYPE)
  @Consumes(JsonApi.JSONAPI_CONTENT_TYPE)
  CompletionStage<Response> populateRepo(
      String generateTemplate,
      @HeaderParam(GlobalConstants.ROUTING_HEADER) String routing,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
      @HeaderParam(GlobalConstants.SERVICE_AUTHORIZATION_HEADER) String serviceAuth,
      @HeaderParam(GlobalConstants.INVOCATION_TYPE) String invocationType,
      @CookieParam(ServiceConstants.GITHUB_SESSION_COOKIE) final String githubToken);
}
