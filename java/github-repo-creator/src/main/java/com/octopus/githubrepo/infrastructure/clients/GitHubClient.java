package com.octopus.githubrepo.infrastructure.clients;

import com.octopus.githubrepo.domain.entities.GitHubPublicKey;
import com.octopus.githubrepo.domain.entities.GitHubSecret;
import com.octopus.githubrepo.domain.entities.GithubRepo;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
@RegisterRestClient
public interface GitHubClient {
  @POST
  @Path("/orgs/{org}/repos")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  Response createRepo(
      GithubRepo repo,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
      @PathParam("org") final String org);

  @GET
  @Path("/orgs/{org}/actions/secrets/public-key")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  GitHubPublicKey getPublicKey(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
      @PathParam("org") final String org);

  @PUT
  @Path("/repos/{owner}/{repo}/actions/secrets/{secret_name}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  Response createSecret(
      GitHubSecret secret,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
      @PathParam("owner") final String owner,
      @PathParam("repo") final String repo,
      @PathParam("secret_name") final String secretName);
}
