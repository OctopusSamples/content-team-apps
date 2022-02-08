package com.octopus.repoclients;

/**
 * Repo clients usually share a base set of credentials, defined once in an implementation of this
 * factory. Then each request will have it's own unique repo and access token, so a new client is
 * created with each request.
 */
public interface RepoClientFactory {

  /**
   * Returns a repo client for a specific repo and credentials.
   *
   * @param repo        The repo to access
   * @param accessToken The access token to use
   * @return A request specific repo client
   */
  RepoClient buildRepoClient(String repo, String accessToken);
}
