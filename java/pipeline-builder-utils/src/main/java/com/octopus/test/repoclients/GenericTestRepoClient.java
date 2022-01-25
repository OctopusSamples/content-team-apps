package com.octopus.test.repoclients;

import io.vavr.control.Try;
import java.util.List;
import lombok.NonNull;

/**
 * A mock repo client for testing pipeline builders.
 */
public class GenericTestRepoClient extends TestRepoClient {

  static int count = 0;

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo The git repo
   */
  public GenericTestRepoClient(final String repo) {
    super(repo, false);
    ++count;
  }

  @Override
  public boolean testFile(String path) {
    return false;
  }

  @Override
  public Try<String> getRepoName() {
    return Try.of(() -> "generic" + count + "application");
  }
}
