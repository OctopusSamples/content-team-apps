package com.octopus.github;

import com.octopus.repoclients.RepoClient;

/**
 * A service that determines when to request a GitHub login.
 */
public interface LoginLogic {

  /**
   * Determine if a login is required.
   *
   * @param repoClient The repo client configured for the supplied repo.
   * @return true if a login is required, false otherwise.
   */
  boolean proceedToLogin(RepoClient repoClient);

  /**
   * Determine if the repo is inaccessible, possibly despite a login.
   *
   * @param repoClient The repo client configured for the supplied repo.
   * @return true if a login is required, false otherwise.
   */
  boolean proceedToError(RepoClient repoClient);
}
