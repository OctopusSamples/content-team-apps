package com.octopus.customers.domain.features;

import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Simple wrapper around a property setting to aid with mocking in tests.
 */
@ApplicationScoped
public class CognitoJwkBase64Feature {
  @ConfigProperty(name = "cognito.jwk-base64")
  Optional<String> cognitoJwk;

  public Optional<String> getCognitoJwk() {
    return cognitoJwk;
  }
}
