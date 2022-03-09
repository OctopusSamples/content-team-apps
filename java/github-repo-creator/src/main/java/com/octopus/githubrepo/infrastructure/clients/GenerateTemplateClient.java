package com.octopus.githubrepo.infrastructure.clients;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import com.octopus.githubrepo.GlobalConstants;

/**
  A REST client accessing the template generator service.
 */
@Path("/api")
@RegisterRestClient
public interface GenerateTemplateClient {
  @POST
  @Path("generatetemplate")
  @Produces("application/zip")
  @Consumes(MediaType.APPLICATION_JSON)
  Response generateTemplate(
      String generateTemplate,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
      @HeaderParam(GlobalConstants.SERVICE_AUTHORIZATION_HEADER) String serviceAuth);
}
