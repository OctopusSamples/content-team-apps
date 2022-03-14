package com.octopus.githubrepo.domain.features;

/**
 * A feature that disables the service by configuring it to return an empty result immediately.
 */
public interface DisableServiceFeature {

  /**
   * Get the disabled flag.
   *
   * @return the disabled flag.
   */
  boolean getDisableRepoCreation();
}
