package com.octopus.githubactions.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.common.collect.ImmutableMap;
import com.octopus.PipelineConstants;
import com.octopus.githubactions.GlobalConstants;
import com.octopus.githubactions.domain.hanlder.SimpleResponse;
import com.octopus.githubactions.domain.hanlder.TemplateHandler;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import com.octopus.lambda.ProxyResponse;
import io.quarkus.logging.Log;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

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

    final String session = lambdaHttpCookieExtractor.getCookieValue(
            input,
            PipelineConstants.SESSION_COOKIE).orElse(null);

    final String routingHeaders = lambdaHttpHeaderExtractor.getFirstHeader(
        input,
        GlobalConstants.ROUTING_HEADER).orElse("");

    final String dataPartitionHeaders = lambdaHttpHeaderExtractor.getFirstHeader(
        input,
        GlobalConstants.DATA_PARTITION).orElse("");

    final String authHeaders = lambdaHttpHeaderExtractor.getFirstHeader(
        input,
        GlobalConstants.AUTHORIZATION_HEADER).orElse("");

    if (lambdaHttpValueExtractor.getQueryParam(input, "action").orElse("").equals("health")) {
      return new ProxyResponse(
          "201",
          "OK",
          new ImmutableMap.Builder<String, String>()
              .put("Content-Type", "text/plain")
              .build());
    }

    try {
      final SimpleResponse response = templateHandler.generatePipeline(
          lambdaHttpValueExtractor.getQueryParam(input, "repo").orElse(""),
          session,
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
    } catch (final Exception ex) {
      Log.error(GlobalConstants.MICROSERVICE_NAME + "-General-Error", ex);
      return new
          ProxyResponse(
          "500",
          "An internal server error was encountered.",
          new ImmutableMap.Builder<String, String>()
              .put("Content-Type", "text/plain")
              .build());
    }
  }

}
