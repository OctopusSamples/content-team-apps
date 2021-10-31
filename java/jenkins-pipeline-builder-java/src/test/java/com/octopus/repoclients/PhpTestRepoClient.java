package com.octopus.repoclients;

import io.vavr.control.Try;

public class PhpTestRepoClient extends TestRepoClient {
  static int count = 0;

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo The git repo
   */
  public PhpTestRepoClient(final String repo) {
    super(repo, false);
    ++count;
  }

  @Override
  public boolean testFile(String path) {
    if (path.endsWith("composer.json")) {
      return true;
    }

    return false;

  }

  @Override
  public Try<String> getRepoName() {
    return Try.of(() -> "php" + count + "application");
  }
}
