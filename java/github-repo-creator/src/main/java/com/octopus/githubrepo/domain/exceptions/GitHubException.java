package com.octopus.githubrepo.domain.exceptions;

/**
 * Represents an exception thrown when working with the GitHub API.
 */
public class GitHubException extends RuntimeException {
  public GitHubException() {
    super();
  }

  public GitHubException(final Throwable ex) {
    super(ex);
  }

  public GitHubException(final String message) {
    super(message);
  }
}
