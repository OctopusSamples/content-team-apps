package com.octopus.githubactions.client;

import com.octopus.githubactions.GlobalConstants;
import com.octopus.githubactions.entities.GitHubEmail;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * A REST client to query GitHub users
 */
@Path("user")
@RegisterRestClient
public interface GitHubUser {

  /**
   * Get the users public email addresses.
   *
   * @param auth The Authorization header.
   * @return The access and refresh tokens.
   */
  @Path("public_emails")
  @Produces(MediaType.APPLICATION_JSON)
  @POST
  GitHubEmail[] publicEmails(@HeaderParam(GlobalConstants.AUTHORIZATION_HEADER) String auth);
}