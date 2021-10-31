package com.octopus.lambda;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableMap;
import com.octopus.builders.PipelineBuilder;
import com.octopus.repoclients.RepoClient;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

/**
 * The AWS Lambda server.
 */
@Named("generate")
public class PipelineLambda implements RequestHandler<Map<String, Object>, ProxyResponse> {

  private static final Logger LOG = Logger.getLogger(PipelineLambda.class.toString());

  @Inject
  RepoClient accessor;

  @Inject
  Instance<PipelineBuilder> builders;

  /**
   * The Lambda entry point.
   *
   * @param input   The JSON object passed in. This is expected to be formatted using proxy
   *                integration.
   * @param context The Lambda context.
   * @return The Lambda proxy integration response.
   */
  @Override
  public ProxyResponse handleRequest(final Map<String, Object> input, final Context context) {
    LOG.log(DEBUG, "PipelineLambda.handleRequest(Map<String,Object>, Context)");
    LOG.log(DEBUG, "input: " + convertObjectToJson(input));
    LOG.log(DEBUG, "context: " + convertObjectToJson(context));

    if (getQueryString(input, "action").equals("health")) {
      return new ProxyResponse(
          "201",
          "OK",
          new ImmutableMap.Builder<String, String>()
              .put("Content-Type", "text/plain")
              .build());
    }

    return generatePipeline(getQueryString(input, "repo"));
  }

  private ProxyResponse generatePipeline(final String repo) {
    if (StringUtils.isBlank(repo)) {
      throw new IllegalArgumentException("repo can not be blank");
    }

    accessor.setRepo(repo);

    final String pipeline = builders.stream()
        .parallel()
        .filter(b -> b.canBuild(accessor))
        .findFirst()
        .map(b -> b.generate(accessor))
        .orElse("No suitable builders were found.");

    return new ProxyResponse(
        "200",
        pipeline,
        new ImmutableMap.Builder<String, String>()
            .put("Content-Type", "text/plain")
            .build());
  }

  private String getQueryString(final Map<String, Object> input, final String query) {
    return Optional
        .ofNullable(input.getOrDefault("queryStringParameters", null))
        .map(Map.class::cast)
        .map(m -> m.getOrDefault(query, null))
        .map(Object::toString)
        .orElse("");
  }

  private String convertObjectToJson(final Object attributes) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(attributes);
    } catch (final JsonProcessingException e) {
      return "";
    }
  }
}
