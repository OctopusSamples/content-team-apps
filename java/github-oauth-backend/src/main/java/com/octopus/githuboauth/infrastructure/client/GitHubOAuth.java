package com.octopus.githuboauth.infrastructure.client;

import com.octopus.githuboauth.domain.oauth.OAuthResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("login/oauth")
@RegisterRestClient
public interface GitHubOAuth {

  /**
   * A REST client to access the audits service.
   */
  @Path("access_token")
  @POST
  OAuthResponse accessToken(
      @QueryParam("client_id") final String clientId,
      @QueryParam("client_secret") final String clientSecret,
      @QueryParam("code") final String code,
      @QueryParam("redirect_uri") final String redirectUri,
      @QueryParam("state") final String state);
}