package com.octopus.serviceaccount.infrastructure.clients;

import com.octopus.serviceaccount.domain.entities.ApiKey;
import com.octopus.serviceaccount.domain.entities.OctopusApiKey;
import com.octopus.serviceaccount.domain.entities.ServiceAccount;
import com.octopus.serviceaccount.domain.entities.Users;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
      @HeaderParam("Cookie") final String cookies,
      @HeaderParam("X-Octopus-Csrf-Token") final String csrf);

  @POST
  @Path("/users/{user}/apikeys")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  OctopusApiKey createApiKey(
      ApiKey apiKey,
      @PathParam("user") final String userId,
      @HeaderParam("Cookie") final String cookies,
      @HeaderParam("X-Octopus-Csrf-Token") final String csrf);

  @GET
  @Path("/users")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  Users getUsers(
      @HeaderParam("Cookie") final String cookies,
      @HeaderParam("X-Octopus-Csrf-Token") final String csrf,
      @QueryParam("filter") final String accountName);
}
