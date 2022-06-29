package com.octopus.githubproxy.infrastructure.clients;

import com.octopus.githubproxy.domain.entities.WorkflowRuns;
import com.octopus.githubproxy.domain.entities.Repo;
import io.smallrye.mutiny.Uni;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
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
  Uni<Repo> getRepo(
      @PathParam("owner") final String owner,
      @PathParam("repo") final String repo,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String auth);

  @GET
  @Path("/repos/{owner}/{repo}/actions/runs")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<WorkflowRuns> getWorkflowRuns(
      @PathParam("owner") final String owner,
      @PathParam("repo") final String repo,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String auth);
}
