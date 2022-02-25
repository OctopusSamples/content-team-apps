package com.octopus.githuboauth.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.common.collect.ImmutableMap;
import com.octopus.githuboauth.OauthBackendConstants;
import com.octopus.githuboauth.domain.handlers.GitHubOauthLoginHandler;
import com.octopus.githuboauth.domain.handlers.SimpleResponse;
import com.octopus.lambda.ProxyResponse;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * This lambda handles the redirection to the GitHub login web page.
 * https://docs.github.com/en/developers/apps/building-github-apps/identifying-and-authorizing-users-for-github-apps#1-request-a-users-github-identity
 */
@Named("login")
public class GitHubOauthLoginLambda implements
    RequestHandler<APIGatewayProxyRequestEvent, ProxyResponse> {

  @Inject
  GitHubOauthLoginHandler gitHubOauthLoginHandler;

  @Override
  public ProxyResponse handleRequest(@Nonnull final APIGatewayProxyRequestEvent input,
      @Nonnull final Context context) {
    final SimpleResponse response = gitHubOauthLoginHandler.oauthLoginRedirect();

    return new ProxyResponse(
        response.getCode().toString(),
        response.getBody(),
        response.getHeaders());
  }
}
