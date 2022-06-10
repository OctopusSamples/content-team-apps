package com.octopus.githubrepo.domain.cognito;

import io.vavr.control.Try;

/**
 * Defines a service that is used to generate access tokens from a Cognito OAuth authentication server.
 */
public interface CognitoAccessTokenGenerator {

  /**
   * Get a access token.
   * @return OAuth access token.
   */
  Try<String> getAccessToken();
}
