package com.octopus.oauth.impl;

import com.octopus.features.OauthClientCreds;
import com.octopus.oauth.OauthClient;
import com.octopus.oauth.OauthClientCredsAccessor;
import io.vavr.control.Try;
import java.util.Base64;
import java.util.Date;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 * A wrapper class that exposes access to a client credentials protected OAuth service.
 */
public class OauthClientCredsAccessorImpl implements OauthClientCredsAccessor {
  private static long expiry;
  private final OauthClientCreds cred;
  private final OauthClient oauthClient;

  private String accessToken;

  /**
   * Constructor.
   *
   * @param cred The credentials feature.
   * @param oauthClient The REST interface.
   */
  public OauthClientCredsAccessorImpl(@NonNull final OauthClientCreds cred, @NonNull final OauthClient oauthClient) {
    this.cred = cred;
    this.oauthClient = oauthClient;
  }

  @Override
  public Try<String> getAccessToken(@NonNull final String scope) {
    if (!StringUtils.isEmpty(accessToken) && new Date().getTime() < expiry) {
      return Try.of(() -> accessToken);
    }

    if (cred.clientId().isPresent() && cred.clientSecret().isPresent()) {
      return Try.of(() -> oauthClient.getToken(
              "Basic " + Base64.getEncoder()
                  .encodeToString(
                      (cred.clientId().get() + ":" + cred.clientSecret().get()).getBytes()),
              CLIENT_CREDENTIALS,
              cred.clientId().get(),
              scope))
          // We expect to see an access token. Fail if the value is empty.
          .filter(oauth -> StringUtils.isNotBlank(oauth.getAccessToken()))
          // We can reuse a token for an hour, but we set the expiry 10 mins before just to be safe.
          .mapTry(oauth -> {
            accessToken = oauth.getAccessToken();
            expiry = new Date().getTime() + ((long) oauth.getExpiresIn() * 1000) - (10 * 60 * 1000);
            return accessToken;
          });
    }

    return Try.failure(new Exception("Cognito client ID or secret were not set"));
  }
}
