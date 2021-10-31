package com.octopus.repoclients;

import io.vavr.control.Try;

public class MavenTestRepoClient extends TestRepoClient {
  static int count = 0;

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo        The git repo
   * @param findWrapper true if this accessor is to report finding a wrapper script,
   */
  public MavenTestRepoClient(final String repo, final boolean findWrapper) {
    super(repo, findWrapper);
    ++count;
  }

  @Override
  public boolean testFile(final String path) {
    if (path.endsWith("pom.xml")) {
      return true;
    }

    if (findWrapper && path.endsWith("mvnw")) {
      return true;
    }

    return false;
  }

  @Override
  public Try<String> getRepoName() {
    return Try.of(() -> "maven" + count + "application");
  }
}
