package com.octopus.githuboauth.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.octopus.githuboauth.domain.handlers.GitHubOauthLoginHandler;
import com.octopus.githuboauth.domain.handlers.SimpleResponse;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;

/**
 * This lambda handles the redirection to the GitHub login web page.
 * https://docs.github.com/en/developers/apps/building-github-apps/identifying-and-authorizing-users-for-github-apps#1-request-a-users-github-identity
 */
@Named("login")
public class GitHubOauthLoginLambda implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @Inject
  GitHubOauthLoginHandler gitHubOauthLoginHandler;

  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      @NonNull final APIGatewayProxyRequestEvent input,
      @NonNull final Context context) {
    final SimpleResponse response = gitHubOauthLoginHandler.oauthLoginRedirect();

    return new APIGatewayProxyResponseEvent()
        .withStatusCode(response.getCode())
        .withBody(response.getBody())
        .withHeaders(response.getHeaders());
  }
}
