package com.octopus.products.domain.features.impl;

import com.octopus.products.domain.features.CognitoClientIdFeature;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Implements the CognitoClientIdFeature feature.
 */
@ApplicationScoped
public class CognitoClientIdFeatureImpl implements CognitoClientIdFeature {

  @ConfigProperty(name = "cognito.client-id")
  String cognitoClientId;

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCognitoClientId() {
    return cognitoClientId;
  }
}
