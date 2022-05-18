package com.octopus.github.impl;

import com.octopus.github.LoginLogic;
import com.octopus.repoclients.RepoClient;
import lombok.NonNull;

/**
 * GitHub has stringent rate limits, and even a registered app will hit these limits after just a few templates are generated.
 * Worse yet, the temple generation code made hit the rate limit mid-generation, meaning the tool can only return a generic
 * template for the end user that just happened to be the one to hit the limit.
 *
 * <p>This login logic implementation requires a login for all repo access, which means that each end user consumes their own rate limit.
 */
public class AlwaysLoginLogic implements LoginLogic {

  /**
   * Determine if a login is required.
   *
   * @param repoClient The repo client configured for the supplied repo.
   * @return true if a login is required, false otherwise.
   */
  @Override
  public boolean proceedToLogin(@NonNull final RepoClient repoClient) {
    return !repoClient.hasAccessToken();
  }

  /**
   * Determine if the repo is inaccessible, possibly despite a login.
   *
   * @param repoClient The repo client configured for the supplied repo.
   * @return true if a login is required, false otherwise.
   */
  @Override
  public boolean proceedToError(@NonNull final RepoClient repoClient) {
    return !repoClient.testRepo();
  }
}
