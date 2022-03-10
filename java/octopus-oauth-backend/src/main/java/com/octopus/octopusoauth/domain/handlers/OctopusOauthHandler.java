package com.octopus.octopusoauth.domain.handlers;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jose.JWSObject;
import com.octopus.PipelineConstants;
import com.octopus.encryption.CryptoUtils;
import com.octopus.http.CookieDateUtils;
import com.octopus.http.FormBodyParser;
import com.octopus.octopusoauth.OauthBackendConstants;
import com.octopus.octopusoauth.domain.oauth.OauthResponse;
import io.quarkus.logging.Log;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * The common logic handling the response from Octofront with the ID token.
 */
@ApplicationScoped
public class OctopusOauthHandler {

  @ConfigProperty(name = "octopus.test.idToken")
  Optional<String> testIdToken;

  @ConfigProperty(name = "octopus.client.redirect")
  String clientRedirect;

  @ConfigProperty(name = "octopus.encryption")
  String octopusEncryption;

  @ConfigProperty(name = "octopus.salt")
  String octopusSalt;

  @Inject
  FormBodyParser formBodyParser;

  @Inject
  CookieDateUtils cookieDateUtils;

  @Inject
  CryptoUtils cryptoUtils;

  /**
   * The common logic handling the OAuth login resdponse.
   *
   * @param body The HTTP POST body.
   * @return A simple HTTP response object to be transformed by the Lambda or HTTP application
   *         layer.
   */
  public SimpleResponse redirectToClient(@NonNull final String body, @NonNull final String nonce) {
    try {
      final Map<String, String> formFields = formBodyParser.parseFormBody(body);

      final OauthResponse response = new OauthResponse(
          formFields.getOrDefault("state", ""),
          formFields.getOrDefault("id_token", ""));

      if (StringUtils.isBlank(response.getIdToken())) {
        throw new Exception("OctopusOauthProxy-Receive-InvalidResponse: ID token can not be blank");
      }

      final JWSObject jwsObject = JWSObject.parse(response.getIdToken());

      if (!nonce.equals(jwsObject.getPayload().toJSONObject().get("nonce").toString())) {
        throw new Exception("OctopusOauthProxy-Receive-NonceMismatch: nonce mismatch");
      }

      if (testIdToken.isEmpty()) {
        return buildResponse(response.getIdToken());
      }

      return buildResponse(testIdToken.get());

    } catch (final Exception ex) {
      Log.error("OctopusOauthProxy-Receive-GeneralError: " + ex);
      return new SimpleResponse(500, "An internal error was detected");
    }
  }

  private SimpleResponse buildResponse(final String idToken) {
    return new SimpleResponse(303, new ImmutableMap.Builder<String, String>()
        .put("Location", clientRedirect)
        .put("Set-Cookie", OauthBackendConstants.OCTOPUS_SESSION_COOKIE + "="
            + cryptoUtils.encrypt(
            idToken,
            octopusEncryption,
            octopusSalt)
            + ";expires=" + cookieDateUtils.getRelativeExpiryDate(2, ChronoUnit.HOURS)
            + ";path=/")
        .build());
  }
}
