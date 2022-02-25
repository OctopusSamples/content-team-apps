package com.octopus.octopusoauth.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.octopus.PipelineConstants;
import com.octopus.encryption.CryptoUtils;
import com.octopus.http.CookieDateUtils;
import com.octopus.http.FormBodyParser;
import com.octopus.octopusoauth.OauthBackendConstants;
import com.octopus.octopusoauth.domain.handlers.OctopusOauthHandler;
import com.octopus.octopusoauth.domain.handlers.SimpleResponse;
import com.octopus.octopusoauth.domain.oauth.OauthResponse;
import io.quarkus.logging.Log;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * This lambda extracts the ID token and passes it back to the client as an encrypted cookie.
 */
public class OctopusOauthRedirectLambda implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @Inject
  OctopusOauthHandler octopusOauthHandler;

  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      @NonNull final APIGatewayProxyRequestEvent input,
      @NonNull final Context context) {

    final SimpleResponse simpleResponse = octopusOauthHandler.getResponseHeaders(input.getBody());

    return new APIGatewayProxyResponseEvent()
        .withStatusCode(simpleResponse.getCode())
        .withBody(simpleResponse.getBody())
        .withHeaders(simpleResponse.getHeaders());
  }

}
