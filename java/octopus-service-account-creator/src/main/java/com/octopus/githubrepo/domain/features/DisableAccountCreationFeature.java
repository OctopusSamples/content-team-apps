package com.octopus.githubrepo.domain.features;

/**
 * A feature that enables or disables account creation.
 */
public interface DisableAccountCreationFeature {

  /**
   * Get the account creation flag.
   *
   * @return The value that enables or disables account creation.
   */
  boolean getDisableAccountCreation();
}
