package com.octopus.githuboauth.domain.handlers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.octopus.PipelineConstants;
import com.octopus.encryption.CryptoUtils;
import com.octopus.githuboauth.OauthBackendConstants;
import com.octopus.githuboauth.domain.oauth.OauthResponse;
import com.octopus.githuboauth.infrastructure.client.GitHubOauth;
import com.octopus.http.CookieDateUtils;
import io.quarkus.logging.Log;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * The common logic to handle a redirection to the GitHub oauth provider.
 */
@ApplicationScoped
public class GitHubOauthRedirect {

  @ConfigProperty(name = "github.client.redirect")
  String clientRedirect;

  @ConfigProperty(name = "github.client.id")
  String clientId;

  @ConfigProperty(name = "github.client.secret")
  String clientSecret;

  @ConfigProperty(name = "github.encryption")
  String githubEncryption;

  @ConfigProperty(name = "github.salt")
  String githubSalt;

  @Inject
  CookieDateUtils cookieDateUtils;

  @RestClient
  GitHubOauth gitHubOauth;

  @Inject
  CryptoUtils cryptoUtils;

  /**
   * Redirect the browser back to the original application.
   *
   * @param state      The OAuth state param.
   * @param savedState The state param saved before authentication.
   * @param code       The OAuth code param.
   * @return A simple HTTP response object to be transformed by the Lambda or HTTP application
   *         layer.
   */
  public SimpleResponse oauthRedirect(@NonNull final String state,
      @NonNull final List<String> savedState, @NonNull final String code) {
    try {

      if (!savedState.contains(state)) {
        return new SimpleResponse(400, "Invalid state parameter");
      }

      final OauthResponse response = gitHubOauth.accessToken(
          clientId,
          clientSecret,
          code,
          clientRedirect);

      if (!StringUtils.isAllBlank(response.getError())) {
        throw new Exception(response.getError() + "\n" + response.getErrorDescription());
      }

      return new SimpleResponse(
          303,
          new ImmutableMap.Builder<String, String>()
              .put("Location", clientRedirect)
              .build(),
          new ImmutableMap.Builder<String, List<String>>()
              .put("Set-Cookie", new ImmutableList.Builder<String>()
                  .add(PipelineConstants.GITHUB_SESSION_COOKIE + "="
                      + cryptoUtils.encrypt(
                      response.getAccessToken(),
                      githubEncryption,
                      githubSalt)
                      + ";expires=" + cookieDateUtils.getRelativeExpiryDate(6, ChronoUnit.HOURS)
                      + ";path=/"
                      + ";HttpOnly")
                  .add(OauthBackendConstants.STATE_COOKIE
                      + "=deleted;expires=Thu, 01 Jan 1970 00:00:00 GMT;HttpOnly;path=/")
                  .build())
              .build());

    } catch (final Exception ex) {
      Log.error("GitHubOauthProxy-Exchange-GeneralError: " + ex);
      return new SimpleResponse(500, "An internal error was detected");
    }
  }
}
