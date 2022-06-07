package com.octopus.githubrepo.infrastructure.clients;

import com.octopus.githubrepo.GlobalConstants;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/** A REST client to access the audits service. */
@Path("api")
@RegisterRestClient
public interface AuditClient {
  @Path("audits")
  @POST
  @Consumes(GlobalConstants.JSONAPI_CONTENT_TYPE)
  @Produces(GlobalConstants.JSONAPI_CONTENT_TYPE)
  String createAudit(
      final String audit,
      @HeaderParam(GlobalConstants.AMAZON_TRACE_ID_HEADER) String xray,
      @HeaderParam(GlobalConstants.ROUTING_HEADER) String routing,
      @HeaderParam(GlobalConstants.DATA_PARTITION) String dataPartition,
      @HeaderParam(GlobalConstants.AUTHORIZATION_HEADER) String auth,
      @HeaderParam(GlobalConstants.SERVICE_AUTHORIZATION_HEADER) String serviceAuth,
      @HeaderParam(GlobalConstants.INVOCATION_TYPE) String invocationType);
}
