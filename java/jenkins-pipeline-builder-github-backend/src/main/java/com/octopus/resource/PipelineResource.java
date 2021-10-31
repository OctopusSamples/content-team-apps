package com.octopus.resource;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.octopus.builders.PipelineBuilder;
import com.octopus.repoclients.RepoClient;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

/**
 * The REST server.
 */
@Path("/pipeline")
public class PipelineResource {

  private static final Logger LOG = Logger.getLogger(PipelineResource.class.toString());

  @Inject
  RepoClient accessor;

  @Inject
  Instance<PipelineBuilder> builders;

  /**
   * Generates a Jenkins pipeline from the given git repository.
   *
   * @param repo The repository URL.
   * @return The Jenkins pipeline.
   */
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String pipeline(@QueryParam("repo") final String repo) {
    LOG.log(DEBUG, "PipelineResource.pipeline(String)");
    LOG.log(DEBUG, "repo: " + repo);

    if (StringUtils.isBlank(repo)) {
      throw new IllegalArgumentException("repo can not be blank");
    }

    accessor.setRepo(repo);

    return builders.stream()
        .filter(b -> b.canBuild(accessor))
        .findFirst()
        .map(b -> b.generate(accessor))
        .orElse("No suitable builders were found.");

  }
}