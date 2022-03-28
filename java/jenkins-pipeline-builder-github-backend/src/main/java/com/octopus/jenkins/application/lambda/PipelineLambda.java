package com.octopus.jenkins.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.collect.ImmutableMap;
import com.octopus.PipelineConstants;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.jenkins.GlobalConstants;
import com.octopus.jenkins.domain.features.impl.MicroserviceNameFeatureImpl;
import com.octopus.jenkins.domain.hanlder.SimpleResponse;
import com.octopus.jenkins.domain.hanlder.TemplateHandler;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import io.quarkus.logging.Log;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * The AWS Lambda server.
 */
@Named("generate")
public class PipelineLambda implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @Inject
  LambdaHttpValueExtractor lambdaHttpValueExtractor;

  @Inject
  LambdaHttpCookieExtractor lambdaHttpCookieExtractor;

  @Inject
  LambdaHttpHeaderExtractor lambdaHttpHeaderExtractor;

  @Inject
  TemplateHandler templateHandler;

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  /**
   * The Lambda entry point.
   *
   * @param input   The JSON object passed in. This is expected to be formatted using proxy
   *                integration.
   * @param context The Lambda context.
   * @return The Lambda proxy integration response.
   */
  @Override
  public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input,
      final Context context) {

    final String session = lambdaHttpCookieExtractor.getCookieValue(
        input,
        PipelineConstants.GITHUB_SESSION_COOKIE).orElse(null);

    final String routingHeaders = lambdaHttpHeaderExtractor.getFirstHeader(
        input,
        GlobalConstants.ROUTING_HEADER).orElse("");

    final String dataPartitionHeaders = lambdaHttpHeaderExtractor.getFirstHeader(
        input,
        GlobalConstants.DATA_PARTITION).orElse("");

    final String authHeaders = lambdaHttpHeaderExtractor.getFirstHeader(
        input,
        GlobalConstants.AUTHORIZATION_HEADER).orElse("");

    final String xray = lambdaHttpHeaderExtractor.getFirstHeader(
        input,
        GlobalConstants.AMAZON_TRACE_ID_HEADER).orElse("");

    if (lambdaHttpValueExtractor.getQueryParam(input, "action").orElse("").equals("health")) {
      return new APIGatewayProxyResponseEvent()
          .withStatusCode(201)
          .withBody("OK")
          .withHeaders(new ImmutableMap.Builder<String, String>()
              .put("Content-Type", "text/plain")
              .build());
    }

    try {
      final SimpleResponse response = templateHandler.generatePipeline(
          lambdaHttpValueExtractor.getQueryParam(input, "repo").orElse(""),
          session,
          xray,
          routingHeaders,
          dataPartitionHeaders,
          authHeaders);

      return new APIGatewayProxyResponseEvent()
          .withStatusCode(response.getCode())
          .withBody(response.getBody())
          .withHeaders(new ImmutableMap.Builder<String, String>()
              .put("Content-Type", "text/plain")
              .build());
    } catch (final Exception ex) {
      Log.error(microserviceNameFeature.getMicroserviceName() + "-General-Error", ex);
      return new APIGatewayProxyResponseEvent()
          .withStatusCode(500)
          .withBody("An internal server error was encountered.")
          .withHeaders(new ImmutableMap.Builder<String, String>()
              .put("Content-Type", "text/plain")
              .build());
    }
  }

}
