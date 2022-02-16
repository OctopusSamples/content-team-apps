package com.octopus.jenkins.application.http;

import com.octopus.PipelineConstants;
import com.octopus.jenkins.GlobalConstants;
import com.octopus.jenkins.domain.exceptions.BadRequest;
import com.octopus.jenkins.domain.exceptions.EntityNotFound;
import com.octopus.jenkins.domain.exceptions.ServerError;
import com.octopus.jenkins.domain.exceptions.Unauthorized;
import com.octopus.jenkins.domain.hanlder.SimpleResponse;
import com.octopus.jenkins.domain.hanlder.TemplateHandler;
import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;

/**
 * The REST server.
 */
@Path("/api/pipeline/jenkins/generate")
public class PipelineResource {

  @Inject
  TemplateHandler templateHandler;

  /**
   * Generates a Github Action Workflow from the given git repository.
   *
   * @param repo The repository URL.
   * @return The GitHub Actions Workflow.
   */
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String pipeline(
      @QueryParam("repo") final String repo,
      @HeaderParam(GlobalConstants.ROUTING_HEADER) final String routingHeaders,
      @HeaderParam(GlobalConstants.DATA_PARTITION) final String dataPartitionHeaders,
      @HeaderParam(GlobalConstants.AUTHORIZATION_HEADER) final String authHeaders,
      @CookieParam(PipelineConstants.SESSION_COOKIE) final String auth) {

    if (StringUtils.isBlank(repo)) {
      throw new BadRequest();
    }

    final SimpleResponse response = templateHandler.generatePipeline(
        repo,
        auth,
        routingHeaders,
        dataPartitionHeaders,
        authHeaders);

    if (response.getCode() == 200) {
      return response.getBody();
    }

    if (response.getCode() == 401) {
      throw new Unauthorized();
    }

    if (response.getCode() == 404) {
      throw new EntityNotFound();
    }

    throw new ServerError();
  }
}