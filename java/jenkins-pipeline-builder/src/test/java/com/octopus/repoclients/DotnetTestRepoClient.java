package com.octopus.repoclients;

import io.vavr.control.Try;
import java.util.List;

public class DotnetTestRepoClient extends TestRepoClient {
  static int count = 0;
  /**
   * A mock repo accessor that pretends to find (or not find) project files and wrapper scripts.
   *
   * @param repo The git repo
   */
  public DotnetTestRepoClient(final String repo) {
    super(repo, false);
    ++count;
  }

  @Override
  public boolean testFile(String path) {
    if (path.endsWith(".sln") || path.endsWith(".csproj")) {
      return true;
    }

    return false;
  }

  @Override
  public Try<String> getFile(final String path) {
    // just enough to fake a dotnet core project
    return path.equals("myproj.csproj")
        ? Try.of(() -> "Sdk=\"Microsoft.NET.Sdk\"")
        : Try.failure(new Exception("file not found"));
  }

  @Override
  public Try<List<String>> getWildcardFiles(final String path) {
    if (path.equals("**/*.csproj")) {
      return Try.of(() -> List.of("myproj.csproj"));
    } else if (path.equals("*.sln")) {
      return Try.of(() -> List.of("myproj.sln"));
    }
    return Try.of(List::of);
  }

  @Override
  public Try<String> getRepoName() {
    return Try.of(() -> "dotnetcore" + count + "application");
  }
}
