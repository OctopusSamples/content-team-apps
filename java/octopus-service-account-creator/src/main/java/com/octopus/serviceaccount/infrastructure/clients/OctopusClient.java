package com.octopus.serviceaccount.infrastructure.clients;

import com.octopus.serviceaccount.domain.entities.ServiceAccount;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * The REST interface to Octopus.
 */
@Path("/api")
@RegisterRestClient
public interface OctopusClient {
  @POST
  @Path("/users/authenticatedToken/OctopusID")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  Response logIn(
      @FormParam("id_token") final String idToken,
      @FormParam("state") final String state,
      @HeaderParam("Cookie") final String cookies);

  @POST
  @Path("/users")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  ServiceAccount createServiceAccount(
      ServiceAccount serviceAccount,
      @HeaderParam("Cookie") final List<String> cookies);
}
