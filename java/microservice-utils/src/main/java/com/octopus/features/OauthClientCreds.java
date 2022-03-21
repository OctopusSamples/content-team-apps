package com.octopus.features;

import java.util.Optional;

/**
 * Defines the authentication details for a client credentials OAuth client.
 */
public interface OauthClientCreds {

  /**
   * The cognito client id.
   *
   * @return The cognito client id..
   */
  Optional<String> clientId();

  /**
   * The cognito client secret.
   *
   * @return The cognito client secret.
   */
  Optional<String> clientSecret();
}
