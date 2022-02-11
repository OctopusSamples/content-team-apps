package com.octopus.githubactions.client;

import com.octopus.githubactions.GlobalConstants;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/** A REST client to access the audits service. */
@Path("api")
@RegisterRestClient
public interface AuditClient {
  @Path("audits")
  @POST
  @Consumes(GlobalConstants.JSON_CONTENT_TYPE)
  String createAudit(
      final String audit,
      @HeaderParam(GlobalConstants.ACCEPT_HEADER) String accept,
      @HeaderParam(GlobalConstants.AUTHORIZATION_HEADER) String auth,
      @HeaderParam(GlobalConstants.SERVICE_AUTHORIZATION_HEADER) String serviceAuth);
}
