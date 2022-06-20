package com.octopus.products.domain.features;

/**
 * A feature exposing the Cognito client ID.
 */
public interface CognitoClientIdFeature {
  /**
   * Returns the cognito client id.
   *
   * @return The Cognito client ID.
   */
  String getCognitoClientId();
}
