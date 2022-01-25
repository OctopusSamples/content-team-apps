package com.octopus.githuboauth.infrastructure.client;

import com.octopus.githuboauth.domain.oauth.OauthResponse;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * A REST client to create an access token.
 */
@Path("login/oauth")
@RegisterRestClient
public interface GitHubOauth {

  /**
   * Transfer the code for an access token.
   *
   * @param clientId The client ID you received from GitHub when you registered.
   * @param clientSecret  The client secret you received from GitHub for your OAuth App.
   * @param code The code used when redirecting users to the GitHub login page.
   * @param redirectUri The URL in your application where users are sent after authorization.
   * @return The access and refresh tokens.
   */
  @Path("access_token")
  @Produces(MediaType.APPLICATION_JSON)
  @POST
  OauthResponse accessToken(
      @QueryParam("client_id") final String clientId,
      @QueryParam("client_secret") final String clientSecret,
      @QueryParam("code") final String code,
      @QueryParam("redirect_uri") final String redirectUri,
      @HeaderParam("accept") final String accept);
}