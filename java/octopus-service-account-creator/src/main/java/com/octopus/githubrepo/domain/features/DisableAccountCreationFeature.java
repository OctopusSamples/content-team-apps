package com.octopus.githubrepo.domain.features;

import java.util.Optional;

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

  /**
   * Get the test API key to use if account creation is disabled.
   *
   * @return test API key.
   */
  Optional<String> getTestApiKey();

  /**
   * Get the test server to use if account creation is disabled.
   *
   * @return test test server.
   */
  Optional<String> getTestServer();
}
