package com.octopus.githubactions.lambda;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.google.common.collect.ImmutableMap;
import com.octopus.PipelineConstants;
import com.octopus.builders.PipelineBuilder;
import com.octopus.encryption.CryptoUtils;
import com.octopus.githubactions.GlobalConstants;
import com.octopus.githubactions.audits.AuditGenerator;
import com.octopus.githubactions.client.AuditClient;
import com.octopus.githubactions.client.CognitoClient;
import com.octopus.githubactions.entities.Audit;
import com.octopus.githubactions.entities.OAuth;
import com.octopus.githubactions.jsonapi.JsonApiConverter;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import com.octopus.lambda.ProxyResponse;
import com.octopus.repoclients.RepoClient;
import com.octopus.repoclients.RepoClientFactory;
import io.vavr.control.Try;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * The AWS Lambda server.
 */
@Named("generate")
public class PipelineLambda implements RequestHandler<APIGatewayProxyRequestEvent, ProxyResponse> {

  private static final Logger LOG = Logger.getLogger(PipelineLambda.class.toString());

  @ConfigProperty(name = "github.encryption")
  String githubEncryption;

  @ConfigProperty(name = "github.salt")
  String githubSalt;

  @Inject
  RepoClientFactory repoClientFactory;

  @Inject
  Instance<PipelineBuilder> builders;

  @Inject
  LambdaHttpValueExtractor lambdaHttpValueExtractor;

  @Inject
  LambdaHttpCookieExtractor lambdaHttpCookieExtractor;

  @Inject
  LambdaHttpHeaderExtractor lambdaHttpHeaderExtractor;

  @Inject
  CryptoUtils cryptoUtils;

  @Inject
  AuditGenerator auditGenerator;

  /**
   * The Lambda entry point.
   *
   * @param input   The JSON object passed in. This is expected to be formatted using proxy
   *                integration.
   * @param context The Lambda context.
   * @return The Lambda proxy integration response.
   */
  @Override
  public ProxyResponse handleRequest(final APIGatewayProxyRequestEvent input,
      final Context context) {
    LOG.log(DEBUG, "PipelineLambda.handleRequest(APIGatewayProxyRequestEvent, Context)");
    LOG.log(DEBUG, "input: " + convertObjectToJson(input));
    LOG.log(DEBUG, "context: " + convertObjectToJson(context));

    if (lambdaHttpValueExtractor.getQueryParam(input, "action").orElse("").equals("health")) {
      return new ProxyResponse(
          "201",
          "OK",
          new ImmutableMap.Builder<String, String>()
              .put("Content-Type", "text/plain")
              .build());
    }

    return generatePipeline(
        lambdaHttpValueExtractor.getQueryParam(input, "repo").orElse(""), input);
  }

  private ProxyResponse generatePipeline(final String repo,
      final APIGatewayProxyRequestEvent input) {
    LOG.log(DEBUG, "PipelineLambda.generatePipeline(String)");
    if (StringUtils.isBlank(repo)) {
      throw new IllegalArgumentException("repo can not be blank");
    }

    final Optional<String> auth = lambdaHttpCookieExtractor.getCookieValue(input,
        PipelineConstants.SESSION_COOKIE);

    final RepoClient accessor = repoClientFactory.buildRepoClient(
        repo,
        auth.map(s -> cryptoUtils.decrypt(s, githubEncryption, githubSalt)).orElse(""));

    return checkForPublicRepo(accessor)
        .orElse(buildPipeline(
            accessor,
            lambdaHttpHeaderExtractor.getAllHeaderParams(
                input,
                GlobalConstants.ACCEPT_HEADER),
            lambdaHttpHeaderExtractor.getAllHeaderParams(
                input,
                GlobalConstants.AUTHORIZATION_HEADER)));
  }

  private ProxyResponse buildPipeline(
      @NonNull final RepoClient accessor,
      @NonNull final List<String> acceptHeaders,
      @NonNull final List<String> authHeaders) {
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
    final String pipeline = builder
        .map(b -> b.generate(accessor))
        .orElse("""
            No suitable builders were found.
            This can happen if no recognised project files were found in the root directory.
            You may still be able to use one of the sample projects from the main page, and customize it to suit your project.
            Click the heading in the top left corner to return to the main page.
            """);

    return new
        ProxyResponse(
        "200",
        pipeline,
        new ImmutableMap.Builder<String, String>()
            .put("Content-Type", "text/plain")
            .build());
  }

  /**
   * If the repo is in accessible it is either because it does not exist, or is a private repo that
   * requires authentication. We make the decision here based on the presence of the session
   * cookie.
   */
  private Optional<ProxyResponse> checkForPublicRepo(@NonNull final RepoClient accessor) {
    if (!accessor.testRepo()) {
      if (accessor.hasAccessToken()) {
        return Optional.of(new ProxyResponse(
            "404",
            accessor.getRepo()
                + " does not appear to be an accessible GitHub repository. Please try a different URL.",
            new ImmutableMap.Builder<String, String>()
                .put("Content-Type", "text/plain")
                .build()));
      } else {
        return Optional.of(new ProxyResponse(
            "401",
            accessor.getRepo()
                + " does not appear to be a public GitHub repository. You must login to GitHub.",
            new ImmutableMap.Builder<String, String>()
                .put("Content-Type", "text/plain")
                .build()));
      }
    }

    return Optional.empty();
  }

  private String convertObjectToJson(final Object attributes) {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(attributes);
    } catch (final JsonProcessingException e) {
      return "";
    }
  }
}
