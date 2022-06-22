package com.octopus.audits.domain.features.impl;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Simple wrapper around a property setting to aid with mocking in tests.
 */
@ApplicationScoped
public class DisableSecurityFeature {
  @ConfigProperty(name = "cognito.disable-auth")
  boolean cognitoDisableAuth;

  public boolean getCognitoAuthDisabled() {
    return cognitoDisableAuth;
  }
}
