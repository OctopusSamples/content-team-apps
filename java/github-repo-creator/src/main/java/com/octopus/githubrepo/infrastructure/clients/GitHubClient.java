package com.octopus.githubrepo.infrastructure.clients;

import com.octopus.githubrepo.domain.entities.GitHubPublicKey;
import com.octopus.githubrepo.domain.entities.GitHubSecret;
import com.octopus.githubrepo.domain.entities.GithubFile;
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

/**
 * The REST client to the GitHub API.
 */
@Path("/")
@RegisterRestClient
public interface GitHubClient {

  @GET
  @Path("/repos/{owner}/{repo}")
  @Produces(MediaType.APPLICATION_JSON)
  void getRepo(
      @PathParam("owner") final String owner,
      @PathParam("repo") final String repo,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String auth);

  @POST
  @Path("/user/repos")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  void createRepo(
      GithubRepo repo,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String auth);

  @PUT
  @Path("/repos/{owner}/{repo}/contents/{file}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  void createFile(
      GithubFile fileContents,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
      @PathParam("owner") final String owner,
      @PathParam("repo") final String repo,
      @PathParam("file") final String file);

  @GET
  @Path("/repos/{owner}/{repo}/contents/{file}")
  @Produces(MediaType.APPLICATION_JSON)
  void getFile(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
      @PathParam("owner") final String owner,
      @PathParam("repo") final String repo,
      @PathParam("file") final String file);

  @GET
  @Path("/repos/{owner}/{repo}/actions/secrets/public-key")
  @Produces(MediaType.APPLICATION_JSON)
  GitHubPublicKey getPublicKey(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
      @PathParam("owner") final String owner,
      @PathParam("repo") final String repo);

  @GET
  @Path("/rate_limit")
  @Produces(MediaType.APPLICATION_JSON)
  Response checkRateLimit(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth);

  @PUT
  @Path("/repos/{owner}/{repo}/actions/secrets/{secret_name}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  void createSecret(
      GitHubSecret secret,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
      @PathParam("owner") final String owner,
      @PathParam("repo") final String repo,
      @PathParam("secret_name") final String secretName);

  @GET
  @Path("/repos/{owner}/{repo}/actions/secrets/{secret_name}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  Response getSecret(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
      @PathParam("owner") final String owner,
      @PathParam("repo") final String repo,
      @PathParam("secret_name") final String secretName);
}
