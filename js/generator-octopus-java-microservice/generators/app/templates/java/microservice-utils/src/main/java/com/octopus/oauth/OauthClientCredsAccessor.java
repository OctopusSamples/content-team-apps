package com.octopus.oauth;

import io.vavr.control.Try;

/**
 * Represents a service used to generate access tokens.
 */
public interface OauthClientCredsAccessor {

  String CLIENT_CREDENTIALS = "client_credentials";

  /**
   * Return the access token, or a wrapped exception if the token could not be generated.
   *
   * @return the access token or a wrapped exception.
   */
  Try<String> getAccessToken(String scope);
}
