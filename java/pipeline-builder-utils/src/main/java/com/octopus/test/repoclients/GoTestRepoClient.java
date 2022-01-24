package com.octopus.test.repoclients;

import io.vavr.control.Try;

/**
 * A mock repo client for testing Go repositories.
 */
public class GoTestRepoClient extends TestRepoClient {
  static int count = 0;

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo The git repo
   */
  public GoTestRepoClient(final String repo, final String branch) {
    super(repo, branch, false);
    ++count;
  }

  @Override
  public boolean testFile(String path) {
    if (path.endsWith("go.mod")) {
      return true;
    }

    return false;
  }

  @Override
  public Try<String> getRepoName() {
    return Try.of(() -> "go" + count + "application");
  }
}
