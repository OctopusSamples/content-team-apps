package com.octopus.githubrepo.domain.features.impl;

import com.octopus.githubrepo.domain.features.DisableServiceFeature;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * An implementation of DisableServiceFeature using Quarkus config files.
 */
@ApplicationScoped
public class DisableServiceFeatureImpl implements DisableServiceFeature {
  @ConfigProperty(name = "github.disable.repo-creation")
  boolean disableRepoCreation;

  @Override
  public boolean getDisableRepoCreation() {
    return disableRepoCreation;
  }
}
