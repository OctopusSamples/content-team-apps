package com.octopus.githubactions.github.application.http;

import com.octopus.PipelineConstants;
import com.octopus.githubactions.github.GlobalConstants;
import com.octopus.githubactions.github.domain.entities.Utms;
import com.octopus.githubactions.github.domain.exceptions.BadRequest;
import com.octopus.githubactions.github.domain.exceptions.EntityNotFound;
import com.octopus.githubactions.github.domain.exceptions.ServerError;
import com.octopus.githubactions.github.domain.exceptions.Unauthorized;
import com.octopus.githubactions.github.domain.hanlder.SimpleResponse;
import com.octopus.githubactions.github.domain.hanlder.TemplateHandler;
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
@Path("/api/pipeline/github/generate")
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
      @HeaderParam(GlobalConstants.AMAZON_TRACE_ID_HEADER) final String xray,
      @HeaderParam(GlobalConstants.ROUTING_HEADER) final String routingHeaders,
      @HeaderParam(GlobalConstants.DATA_PARTITION) final String dataPartitionHeaders,
      @HeaderParam(GlobalConstants.AUTHORIZATION_HEADER) final String authHeaders,
      @CookieParam(PipelineConstants.GITHUB_SESSION_COOKIE) final String auth,
      @QueryParam("utm_source") final String source,
      @QueryParam("utm_medium") final String medium,
      @QueryParam("utm_campaign") final String campaign,
      @QueryParam("utm_term") final String term,
      @QueryParam("utm_content") final String content) {

    if (StringUtils.isBlank(repo)) {
      throw new BadRequest();
    }

    final SimpleResponse response = templateHandler.generatePipeline(
        repo,
        auth,
        xray,
        routingHeaders,
        dataPartitionHeaders,
        authHeaders,
        Utms.builder()
            .source(source)
            .medium(medium)
            .campaign(campaign)
            .term(term)
            .content(content)
            .build());

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