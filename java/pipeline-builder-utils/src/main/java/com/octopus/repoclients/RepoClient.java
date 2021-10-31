package com.octopus.repoclients;

import io.vavr.control.Try;
import java.util.List;

/**
 * An abstraction for accessing files in a repo.
 */
public interface RepoClient {

  void setRepo(String repo);

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
   * @return the list of matching files.
   */
  Try<List<String>> getWildcardFiles(String path);

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
}
