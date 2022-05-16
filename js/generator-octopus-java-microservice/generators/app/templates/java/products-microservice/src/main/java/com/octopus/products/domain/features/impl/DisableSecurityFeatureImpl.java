package com.octopus.products.domain.features.impl;

import com.octopus.features.DisableSecurityFeature;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Simple wrapper around a property setting to aid with mocking in tests.
 */
@ApplicationScoped
public class DisableSecurityFeatureImpl implements DisableSecurityFeature {
  @ConfigProperty(name = "cognito.disable-auth")
  boolean cognitoDisableAuth;

  public boolean getCognitoAuthDisabled() {
    return cognitoDisableAuth;
  }
}
