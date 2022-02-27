package com.octopus.githuboauth.domain.handlers;

import com.google.common.collect.ImmutableMap;
import com.octopus.githuboauth.OauthBackendConstants;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * The common logic hanlding the response from a GitHub oauth login.
 */
@ApplicationScoped
public class GitHubOauthLoginHandler {
  private static final String GitHubAuthURL = "https://github.com/login/oauth/authorize";

  @ConfigProperty(name = "github.client.id")
  String clientId;

  @ConfigProperty(name = "github.login.redirect")
  String loginRedirect;

  /**
   * Handle the oauth login response by encrypting the token in a cookie.
   *
   * @return A simple HTTP response object to be transformed by the Lambda or HTTP application layer.
   */
  public SimpleResponse oauthLoginRedirect() {
    final String state = UUID.randomUUID().toString();
    /*
      Redirect to the GitHub login.
      Saving state values as cookies was inspired by the CodeAuthenticationMechanism class
      https://github.com/quarkusio/quarkus/blob/main/extensions/oidc/runtime/src/main/java/io/quarkus/oidc/runtime/CodeAuthenticationMechanism.java#L253
     */
    return new SimpleResponse(
        307,
        null,
        new ImmutableMap.Builder<String, String>()
            .put("Location", GitHubAuthURL
                + "?client_id=" + clientId
                + "&redirect_uri=" + loginRedirect
                /*
                  Yikes! There is no such thing as read only repo access:
                  https://github.com/github/feedback/discussions/7891
                 */
                + "&scope=user:email%20repo"
                + "&state=" + state
                + "&allow_signup=false")
            .put("Set-Cookie", OauthBackendConstants.STATE_COOKIE + "=" + state + ";HttpOnly;path=/")
            .build());
  }
}
