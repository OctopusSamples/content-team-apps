package com.octopus.octopusoauth.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import com.octopus.octopusoauth.domain.handlers.OctopusOauthLoginHandler;
import com.octopus.octopusoauth.domain.handlers.OctopusOauthRedirectHandler;
import com.octopus.octopusoauth.domain.handlers.SimpleResponse;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;

/**
 * This lambda extracts the ID token and passes it back to the client as an encrypted cookie.
 */
@Named("login")
public class OctopusOauthLoginLambda implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @Inject
  OctopusOauthLoginHandler octopusOauthLoginHandler;

  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      @NonNull final APIGatewayProxyRequestEvent input,
      @NonNull final Context context) {

    final SimpleResponse simpleResponse = octopusOauthLoginHandler.redirectToLogin();

    return new APIGatewayProxyResponseEvent()
        .withStatusCode(simpleResponse.getCode())
        .withBody(simpleResponse.getBody())
        .withHeaders(simpleResponse.getHeaders());
  }

}
