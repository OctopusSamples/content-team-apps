package com.octopus.githubrepo.domain.features.impl;

import com.octopus.githubrepo.domain.features.DisableAccountCreationFeature;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Simple wrapper around a property setting that enables or disables account creation.
 */
@ApplicationScoped
public class DisableAccountCreationFeatureImpl implements DisableAccountCreationFeature {
  @ConfigProperty(name = "octopus.disable.account-creation")
  boolean disableAccountCreation;

  @ConfigProperty(name = "octopus.test-api-key")
  Optional<String> testApiKey;

  @ConfigProperty(name = "octopus.test-server")
  Optional<String> testServer;

  @Override
  public boolean getDisableAccountCreation() {
    return disableAccountCreation;
  }

  @Override
  public Optional<String> getTestApiKey() {
    return testApiKey;
  }

  @Override
  public Optional<String> getTestServer() {
    return testServer;
  }
}
