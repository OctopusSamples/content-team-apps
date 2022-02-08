package com.octopus.repoclients.impl;

import com.octopus.http.ReadOnlyHttpClient;
import com.octopus.repoclients.RepoClient;
import com.octopus.repoclients.RepoClientFactory;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * An implementation of RepoClientFactory that creates GithubRepoClients.
 */
@Builder
public class GitHubRepoClientFactory implements RepoClientFactory {

  @Getter
  @Setter
  private ReadOnlyHttpClient readOnlyHttpClient;

  @Getter
  @Setter
  private String username;

  @Getter
  @Setter
  private String password;

  /**
   * {@inheritDoc}
   */
  public RepoClient buildRepoClient(@NonNull final String repo, final String accessToken) {
    return new GithubRepoClient(repo, readOnlyHttpClient, username, password, accessToken);
  }
}
