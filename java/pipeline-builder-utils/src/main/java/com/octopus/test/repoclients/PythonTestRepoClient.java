package com.octopus.test.repoclients;

import io.vavr.control.Try;

/**
 * A mock repo client for testing Python repositories.
 */
public class PythonTestRepoClient extends TestRepoClient {
  static int count = 0;

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo The git repo
   */
  public PythonTestRepoClient(final String repo, final String branch) {
    super(repo, branch, false);
    ++count;
  }

  @Override
  public boolean testFile(String path) {
    if (path.endsWith("requirements.txt")) {
      return true;
    }

    return false;
  }

  @Override
  public Try<String> getRepoName() {
    return Try.of(() -> "python" + count + "application");
  }
}
