package com.octopus.repoclients;

import io.vavr.control.Try;
import java.util.List;
import lombok.NonNull;

/**
 * An abstraction for accessing files in a repo.
 */
public interface RepoClient {

  /**
   * Set the Repo URL.
   * @param repo The repo URL.
   */
  void setRepo(String repo);

  /**
   * Gets the Repo URL.
   * @returns repo The repo URL.
   */
  String getRepo();

  /**
   * Set the access token.
   * @param accessToken The access token.
   */
  void setAccessToken(String accessToken);

  /**
   * Determine if the access token is set.
   * @returns True if an access token is defined, and false otherwise.
   */
  boolean hasAccessToken();

  /**
   * Returns the contents of a file from the given path.
   *
   * @param path The repo file path
   * @return The file contents
   */
  Try<String> getFile(String path);

  /**
   * Returns true if a file exists.
   *
   * @param path The path to test
   * @return true if the file exists, and false otherwise.
   */
  boolean testFile(String path);

  /**
   * Returns the list of files that match a wildcard path.
   *
   * @param path The path to test
   * @param limit Limit the number of results
   * @return the list of matching files.
   */
  Try<List<String>> getWildcardFiles(String path, int limit);

  /**
   * Returns true if any files match the supplied path.
   *
   * @param path The path to test
   * @return true if any files match, and false otherwise
   */
  Try<Boolean> wildCardFileExist(@NonNull final String path);

  /**
   * Returns the path to the repository, suitable for performing a clone operation.
   *
   * @return The repo path.
   */
  String getRepoPath();

  /**
   * Returns the default branches of the repo (or guessing what the branches would be if hitting an
   * API rate limit).
   *
   * @return The default branches.
   */
  List<String> getDefaultBranches();

  /**
   * Returns the name of the repo.
   *
   * @return The name of the repo.
   */
  Try<String> getRepoName();

  /**
   * Tests the supplied url.
   *
   * @return true if the url was a valid repo, and false otherwise
   */
  boolean testRepo();
}
