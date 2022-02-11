package com.octopus.githubactions.resource;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.octopus.builders.PipelineBuilder;
import com.octopus.githubactions.GlobalConstants;
import com.octopus.githubactions.audits.AuditGenerator;
import com.octopus.githubactions.entities.Audit;
import com.octopus.repoclients.RepoClient;
import com.octopus.repoclients.RepoClientFactory;
import java.util.List;
import java.util.Optional;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

/**
 * The REST server.
 */
@Path("/api/pipeline/github/generate")
public class PipelineResource {

  private static final Logger LOG = Logger.getLogger(PipelineResource.class.toString());

  @Inject
  RepoClientFactory repoClientFactory;

  @Inject
  Instance<PipelineBuilder> builders;

  @Inject
  AuditGenerator auditGenerator;

  /**
   * Generates a Jenkins pipeline from the given git repository.
   *
   * @param repo The repository URL.
   * @return The Jenkins pipeline.
   */
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String pipeline(
      @QueryParam("repo") final String repo,
      @HeaderParam(GlobalConstants.ACCEPT_HEADER) final List<String> acceptHeaders,
      @HeaderParam(GlobalConstants.AUTHORIZATION_HEADER) final List<String> authHeaders) {
    LOG.log(DEBUG, "PipelineResource.pipeline(String)");
    LOG.log(DEBUG, "repo: " + repo);

    if (StringUtils.isBlank(repo)) {
      throw new IllegalArgumentException("repo can not be blank");
    }

    final RepoClient accessor = repoClientFactory.buildRepoClient(repo, null);

    // Get the builder
    final Optional<PipelineBuilder> builder = builders.stream()
        .sorted((o1, o2) -> o2.getPriority().compareTo(o1.getPriority()))
        .parallel()
        .filter(b -> b.canBuild(accessor))
        .findFirst();

    // Write an audit message
    builder.ifPresent(b ->
        auditGenerator.createAuditEvent(new Audit(
                GlobalConstants.MICROSERVICE_NAME,
                GlobalConstants.CREATED_TEMPLATE_ACTION,
                b.getClass().getName()),
            acceptHeaders,
            authHeaders)
    );

    // Return the template
    return builder
        .map(b -> b.generate(accessor))
        .orElse("""
            No suitable builders were found.
            This can happen if no recognised project files were found in the root directory.
            You may still be able to use one of the sample projects from the main page, and customize it to suit your project.
            Click the heading in the top left corner to return to the main page.
            """);
  }
}