package com.octopus.githubactions.application.lambda;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.octopus.PipelineConstants;
import com.octopus.builders.PipelineBuilder;
import com.octopus.encryption.AsymmetricEncryptor;
import com.octopus.encryption.CryptoUtils;
import com.octopus.githubactions.GlobalConstants;
import com.octopus.githubactions.domain.audits.AuditGenerator;
import com.octopus.githubactions.domain.hanlder.SimpleResponse;
import com.octopus.githubactions.domain.hanlder.TemplateHandler;
import com.octopus.githubactions.infrastructure.client.GitHubUser;
import com.octopus.githubactions.domain.entities.Audit;
import com.octopus.githubactions.domain.entities.GitHubEmail;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import com.octopus.lambda.ProxyResponse;
import com.octopus.repoclients.RepoClient;
import com.octopus.repoclients.RepoClientFactory;
import io.quarkus.logging.Log;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

/**
 * The AWS Lambda server.
 */
@Named("generate")
public class PipelineLambda implements RequestHandler<APIGatewayProxyRequestEvent, ProxyResponse> {

  @Inject
  LambdaHttpValueExtractor lambdaHttpValueExtractor;

  @Inject
  LambdaHttpCookieExtractor lambdaHttpCookieExtractor;

  @Inject
  LambdaHttpHeaderExtractor lambdaHttpHeaderExtractor;

  @Inject
  TemplateHandler templateHandler;

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

    final String auth = lambdaHttpCookieExtractor.getCookieValue(
            input,
            PipelineConstants.SESSION_COOKIE).orElse(null);

    final List<String> routingHeaders = lambdaHttpHeaderExtractor.getAllHeaders(
        input,
        GlobalConstants.ROUTING_HEADER);

    final List<String> dataPartitionHeaders = lambdaHttpHeaderExtractor.getAllHeaders(
        input,
        GlobalConstants.DATA_PARTITION);

    final List<String> authHeaders = lambdaHttpHeaderExtractor.getAllHeaders(
        input,
        GlobalConstants.AUTHORIZATION_HEADER);

    if (lambdaHttpValueExtractor.getQueryParam(input, "action").orElse("").equals("health")) {
      return new ProxyResponse(
          "201",
          "OK",
          new ImmutableMap.Builder<String, String>()
              .put("Content-Type", "text/plain")
              .build());
    }

    final SimpleResponse response = templateHandler.generatePipeline(
        lambdaHttpValueExtractor.getQueryParam(input, "repo").orElse(""),
        auth,
        routingHeaders,
        dataPartitionHeaders,
        authHeaders);

    return new
        ProxyResponse(
        String.valueOf(response.getCode()),
        response.getBody(),
        new ImmutableMap.Builder<String, String>()
            .put("Content-Type", "text/plain")
            .build());
  }

}
