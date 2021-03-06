package com.octopus.githubrepo.infrastructure.clients;

import com.octopus.Constants;
import com.octopus.githubrepo.GlobalConstants;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
  A REST client accessing the template generator service.
 */
@Path("/api")
@RegisterRestClient
public interface GenerateTemplateClient {
  @POST
  @Path("generatetemplate")
  @Produces("application/zip")
  @Consumes(Constants.JsonApi.JSONAPI_CONTENT_TYPE)
  Response generateTemplate(
      String generateTemplate,
      @HeaderParam(GlobalConstants.ROUTING_HEADER) String routing,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
      @HeaderParam(GlobalConstants.SERVICE_AUTHORIZATION_HEADER) String serviceAuth);
}
