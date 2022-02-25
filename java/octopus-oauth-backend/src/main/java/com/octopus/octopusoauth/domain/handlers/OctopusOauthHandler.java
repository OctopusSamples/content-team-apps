package com.octopus.octopusoauth.domain.handlers;

import com.google.common.collect.ImmutableMap;
import com.octopus.PipelineConstants;
import com.octopus.encryption.CryptoUtils;
import com.octopus.http.CookieDateUtils;
import com.octopus.http.FormBodyParser;
import com.octopus.octopusoauth.domain.oauth.OauthResponse;
import io.quarkus.logging.Log;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class OctopusOauthHandler {
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

  public SimpleResponse getResponseHeaders(@NonNull final String body) {
    try {
      final Map<String, String> formFields = formBodyParser.parseFormBody(body);

      final OauthResponse response = new OauthResponse(
          formFields.getOrDefault("state", ""),
          formFields.getOrDefault("id_token", ""));

      if (StringUtils.isBlank(response.getIdToken())) {
        throw new Exception("ID token can not be blank");
      }

      return new SimpleResponse(303, new ImmutableMap.Builder<String, String>()
          .put("Location", clientRedirect)
          .put("Set-Cookie", PipelineConstants.SESSION_COOKIE + "="
                  + cryptoUtils.encrypt(
                  response.getIdToken(),
                  octopusEncryption,
                  octopusSalt)
                  + ";expires=" + cookieDateUtils.getRelativeExpiryDate(2, ChronoUnit.HOURS)
                  + ";path=/"
                  + ";HttpOnly")
          .build());

    } catch (final Exception ex) {
      Log.error("GitHubOauthProxy-Exchange-GeneralError: " + ex);
      return new SimpleResponse(500, "An internal error was detected");
    }
  }
}
