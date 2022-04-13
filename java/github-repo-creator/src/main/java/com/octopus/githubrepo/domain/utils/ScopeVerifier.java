package com.octopus.githubrepo.domain.utils;

/**
 * Defaines a services used to verify the scopes on a GitHub token.
 */
public interface ScopeVerifier {

  /**
   * Verifies the scopes on a GitHub token.
   *
   * @param decryptedGithubToken the GitHub token.
   */
  void verifyScopes(final String decryptedGithubToken);
}
