package com.octopus.githubactions.github.infrastructure.client;

import com.octopus.githubactions.github.GlobalConstants;
import com.octopus.githubactions.github.domain.entities.GitHubEmail;
import com.octopus.githubactions.github.domain.entities.GitHubUser;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * A REST client to query GitHub users.
 */
@RegisterRestClient
@Path("/")
public interface GitHubApi {

  /**
   * Get the users public email addresses.
   *
   * @param auth The Authorization header.
   * @return The access and refresh tokens.
   */
  @Path("user/public_emails")
  @Produces(MediaType.APPLICATION_JSON)
  @GET
  GitHubEmail[] publicEmails(@HeaderParam(GlobalConstants.AUTHORIZATION_HEADER) String auth);

  /**
   * Get the users details.
   *
   * @param auth The Authorization header.
   * @return The users details.
   */
  @Path("user")
  @Produces(MediaType.APPLICATION_JSON)
  @GET
  GitHubUser user(@HeaderParam(GlobalConstants.AUTHORIZATION_HEADER) String auth);
}