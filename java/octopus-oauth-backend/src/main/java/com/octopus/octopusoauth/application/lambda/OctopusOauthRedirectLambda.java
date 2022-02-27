package com.octopus.octopusoauth.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.octopus.octopusoauth.domain.handlers.OctopusOauthHandler;
import com.octopus.octopusoauth.domain.handlers.SimpleResponse;
import javax.inject.Inject;
import lombok.NonNull;

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

    final SimpleResponse simpleResponse = octopusOauthHandler.redirectToClient(input.getBody());

    return new APIGatewayProxyResponseEvent()
        .withStatusCode(simpleResponse.getCode())
        .withBody(simpleResponse.getBody())
        .withHeaders(simpleResponse.getHeaders());
  }

}
