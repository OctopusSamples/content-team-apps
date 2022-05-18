package com.octopus.github.impl;

import com.octopus.github.LoginLogic;
import com.octopus.repoclients.RepoClient;
import lombok.NonNull;

/**
 * Login logic implementation that attempts to detect private repos and requests a login for them.
 */
public class PrivateRepoLoginLogic implements LoginLogic {

  /**
   * Determine if a login is required.
   *
   * @param repoClient The repo client configured for the supplied repo.
   * @return true if a login is required, false otherwise.
   */
  @Override
  public boolean proceedToLogin(@NonNull final RepoClient repoClient) {
    return !repoClient.testRepo() && !repoClient.hasAccessToken();
  }

  /**
   * Determine if the repo is inaccessible, possibly despite a login.
   *
   * @param repoClient The repo client configured for the supplied repo.
   * @return true if a login is required, false otherwise.
   */
  @Override
  public boolean proceedToError(RepoClient repoClient) {
    return !repoClient.testRepo();
  }
}
