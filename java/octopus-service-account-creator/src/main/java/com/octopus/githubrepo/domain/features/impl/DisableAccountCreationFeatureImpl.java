package com.octopus.githubrepo.domain.features.impl;

import com.octopus.githubrepo.domain.features.DisableAccountCreationFeature;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Simple wrapper around a property setting that enables or disables account creation.
 */
@ApplicationScoped
public class DisableAccountCreationFeatureImpl implements DisableAccountCreationFeature {
  @ConfigProperty(name = "octopus.disable.account-creation")
  boolean disableAccountCreation;

  @Override
  public boolean getDisableAccountCreation() {
    return disableAccountCreation;
  }
}
