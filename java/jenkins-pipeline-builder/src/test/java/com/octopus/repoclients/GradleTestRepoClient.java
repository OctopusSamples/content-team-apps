package com.octopus.repoclients;

import io.vavr.control.Try;

public class GradleTestRepoClient extends TestRepoClient {
  static int count = 0;
  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo        The git repo
   * @param findWrapper true if this accessor is to report finding a wrapper script,
   */
  public GradleTestRepoClient(final String repo, final boolean findWrapper) {
    super(repo, findWrapper);
    ++count;
  }

  @Override
  public boolean testFile(final String path) {
    if (path.endsWith("build.gradle") || path.endsWith("build.gradle.kts")) {
      return true;
    }

    if (findWrapper && path.endsWith("gradlew")) {
      return true;
    }

    return false;
  }

  @Override
  public Try<String> getRepoName() {
    return Try.of(() -> "gradle" + count + "application");
  }
}
