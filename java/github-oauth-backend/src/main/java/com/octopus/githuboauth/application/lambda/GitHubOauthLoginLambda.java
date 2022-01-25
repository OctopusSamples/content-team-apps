package com.octopus.githuboauth.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.common.collect.ImmutableMap;
import com.octopus.githuboauth.Constants;
import com.octopus.lambda.ProxyResponse;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * This lambda handles the redirection to the GitHub login web page.
 * https://docs.github.com/en/developers/apps/building-github-apps/identifying-and-authorizing-users-for-github-apps#1-request-a-users-github-identity
 */
@Named("login")
public class GitHubOauthLoginLambda implements RequestHandler<Map<String, Object>, ProxyResponse> {

  private static final String GitHubAuthURL = "https://github.com/login/oauth/authorize";

  @ConfigProperty(name = "github.client.id")
  String clientId;

  @ConfigProperty(name = "github.login.redirect")
  String loginRedirect;

  @Override
  public ProxyResponse handleRequest(@Nonnull final Map<String, Object> stringObjectMap, @Nonnull final Context context) {
    final String state = UUID.randomUUID().toString();
    // Redirect to the GitHub login
    return new ProxyResponse(
        "307",
        null,
        new ImmutableMap.Builder<String, String>()
            .put("Location", GitHubAuthURL
                + "?client_id=" + clientId
                + "&redirect_uri=" + loginRedirect
                + "&state=" + state
                + "&allow_signup=false")
            .put("Set-Cookie", Constants.STATE_COOKIE + "=" + state)
            .build());
  }
}
