package com.octopus.githubactions.client;

import com.octopus.githubactions.GlobalConstants;
import com.octopus.githubactions.entities.OAuth;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestForm;

/** A REST client to access the audits service. */
@Path("oauth")
@RegisterRestClient
public interface CognitoClient {
  @Path("token")
  @POST
  @Consumes(GlobalConstants.FROM_ENCODED_CONTENT_TYPE)
  @Produces(GlobalConstants.JSON_CONTENT_TYPE)
  OAuth getToken(
      @HeaderParam("Authorization") final String authorization,
      @RestForm("grant_type") final String grantType,
      @RestForm("client_id") final String clientId,
      @RestForm("scope") final String scope);
}
