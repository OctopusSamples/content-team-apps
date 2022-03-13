package com.octopus.octopusoauth.domain.handlers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.nimbusds.jose.JWSObject;
import com.octopus.encryption.CryptoUtils;
import com.octopus.http.CookieDateUtils;
import com.octopus.http.FormBodyParser;
import com.octopus.octopusoauth.OauthBackendConstants;
import com.octopus.octopusoauth.domain.oauth.OauthResponse;
import io.quarkus.logging.Log;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * The common logic handling the response from Octofront with the ID token.
 */
@ApplicationScoped
public class OctopusOauthLoginHandler {

  @ConfigProperty(name = "octopus.login.redirect")
  String loginRedirect;

  @ConfigProperty(name = "octopus.client.redirect")
  String clientRedirect;

  @ConfigProperty(name = "octopus.disable.login")
  boolean disableLogin;

  @ConfigProperty(name = "octopus.encryption")
  String octopusEncryption;

  @ConfigProperty(name = "octopus.salt")
  String octopusSalt;

  @ConfigProperty(name = "octopus.test.idToken")
  Optional<String> testIdToken;

  @Inject
  CookieDateUtils cookieDateUtils;

  @Inject
  CryptoUtils cryptoUtils;

  /**
   * The common logic handling the OAuth login redirection.
   *
   * @return A simple HTTP response object to be transformed by the Lambda or HTTP application
   *         layer.
   */
  public SimpleResponse redirectToLogin() {
    // When login is disabled, we return immediately to the web app
    if (disableLogin) {
      return buildResponse(testIdToken.orElse(null));
    }

    final String nonce = UUID.randomUUID().toString();
    /*
      Redirect to the GitHub login.
      Saving state values as cookies was inspired by the CodeAuthenticationMechanism class
      https://github.com/quarkusio/quarkus/blob/main/extensions/oidc/runtime/src/main/java/io/quarkus/oidc/runtime/CodeAuthenticationMechanism.java#L253
     */
    return new SimpleResponse(
        307,
        null,
        new ImmutableMap.Builder<String, String>()
            .put("Location", "https://octopus.com/oauth2/authorize?"
                    + "client_id=855b8e5a-c3c4-4c4d-91b1-fef5dd762ec2&scope=openid%20profile%20email"
                    + "&response_type=code+id_token"
                    + "&response_mode=form_post"
                    + "&nonce=" + nonce
                    + "&redirect_uri=" + loginRedirect)
            .put("Set-Cookie", OauthBackendConstants.STATE_COOKIE + "=" + nonce
                + ";expires=" + cookieDateUtils.getRelativeExpiryDate(1, ChronoUnit.HOURS)
                + ";HttpOnly;path=/")
            .build());
  }

  private SimpleResponse buildResponse(final String idToken) {
    final Builder<String, String> map = new ImmutableMap.Builder<String, String>()
        .put("Location", clientRedirect);

    if (StringUtils.isNotBlank(idToken)) {
      map.put("Set-Cookie", OauthBackendConstants.OCTOPUS_SESSION_COOKIE + "="
          + cryptoUtils.encrypt(
          idToken,
          octopusEncryption,
          octopusSalt)
          + ";expires=" + cookieDateUtils.getRelativeExpiryDate(2, ChronoUnit.HOURS)
          + ";path=/");
    }

    return new SimpleResponse(307, map.build());
  }
}
