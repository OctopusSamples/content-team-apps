package com.octopus.test.repoclients;

import com.octopus.repoclients.RepoClient;
import io.vavr.control.Try;
import java.util.List;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 * A base class for mock repo clients.
 */
public abstract class TestRepoClient implements RepoClient {

  private final String branch;
  protected boolean findWrapper;
  private String repo;
  private String accessToken;

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo        The git repo
   * @param findWrapper true if this accessor is to report finding a wrapper script, and false
   *                    otherwise
   */
  public TestRepoClient(final String repo, final String branch, boolean findWrapper) {
    this.repo = repo;
    this.findWrapper = findWrapper;
    this.branch = branch;
  }

  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo        The git repo
   * @param findWrapper true if this accessor is to report finding a wrapper script, and false
   *                    otherwise
   */
  public TestRepoClient(final String repo, boolean findWrapper) {
    this.repo = repo;
    this.findWrapper = findWrapper;
    this.branch = "master";
  }

  @Override
  public String getRepo() {
    return repo;
  }

  @Override
  public boolean hasAccessToken() {
    return StringUtils.isNoneBlank(accessToken);
  }

  @Override
  public Try<String> getFile(String path) {
    return Try.of(() -> "");
  }

  @Override
  public String getRepoPath() {
    return repo;
  }

  @Override
  public List<String> getDefaultBranches() {
    return List.of(branch);
  }

  @Override
  public Try<List<String>> getWildcardFiles(String path, int limit) {
    return Try.of(List::of);
  }

  @Override
  public Try<Boolean> wildCardFileExist(@NonNull final String path) {
    return Try.failure(new Exception("not implemented"));
  }

  @Override
  public Try<String> getRepoName() {
    return Try.failure(new Exception("not implemented"));
  }

  @Override
  public boolean testRepo() {
    return true;
  }
}
