package com.octopus.repoclients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.http.ReadOnlyHttpClient;
import com.octopus.http.impl.ReadOnlyHttpClientImpl;
import com.octopus.repoclients.impl.GithubRepoClient;
import com.octopus.repoclients.impl.GithubRepoDetails;
import io.vavr.control.Try;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


public class GithubRepoClientTest {

  private static final ReadOnlyHttpClient HTTP_CLIENT = new ReadOnlyHttpClientImpl();

  @ParameterizedTest
  @CsvSource({
      "https://github.com/VScode/vscode.github.io,VScode,vscode.github.io,true",
      "https://github.com/VScode/vscode,VScode,vscode,true",
      "https://github.com/a/b.git,a,b,true",
      "https://github.com/a/b.git.a,a,b.git.a,true",
      "https://github.com/VScode,,,false"
  })
  public void regexTests(final String url, final String username, final String repo, final boolean matches) {
    final Matcher matcher = Pattern.compile(GithubRepoClient.GITHUB_REGEX).matcher(url);
    assertEquals(matcher.matches(), matches);
    if (matcher.matches()) {
      assertEquals(username, matcher.group("username"));
      assertEquals(repo, matcher.group("repo"));
    }
  }

  @Test
  public void testRepoScanning() {
    final Try<List<String>> files = GithubRepoClient.builder()
        .readOnlyHttpClient(HTTP_CLIENT)
        .repo("https://github.com/OctopusSamples/RandomQuotes")
        .build()
        .getWildcardFiles("*.sln", 1);

    assertTrue(files.isSuccess());
    assertTrue(files.get().contains("RandomQuotes.sln"));
  }

  @ParameterizedTest
  @CsvSource({
      "https://github.com/OctopusSamples/RandomQuotes,OctopusSamples,RandomQuotes",
      "https://github.com/OctopusSamples/RandomQuotes ,OctopusSamples,RandomQuotes",
      " https://github.com/OctopusSamples/RandomQuotes ,OctopusSamples,RandomQuotes",
      "https://github.com/OctopusSamples/RandomQuotes/,OctopusSamples,RandomQuotes",
      " https://github.com/OctopusSamples/RandomQuotes/,OctopusSamples,RandomQuotes",
      "https://github.com/OctopusSamples/RandomQuotes/ ,OctopusSamples,RandomQuotes",
      "https://github.com/OctopusSamples/RandomQuotes/blah,OctopusSamples,RandomQuotes",
      "https://github.com/OctopusSamples/RandomQuotes/blah,OctopusSamples ,RandomQuotes",
      " https://github.com/OctopusSamples/RandomQuotes/blah,OctopusSamples,RandomQuotes",
      "https://github.com/OctopusSamples/RandomQuotes.git,OctopusSamples,RandomQuotes",
      "https://github.com/OctopusSamples/RandomQuotes.git,OctopusSamples,RandomQuotes ",
      " https://github.com/OctopusSamples/RandomQuotes.git,OctopusSamples,RandomQuotes",
  })
  public void testUrlMatching(final String url, final String username, final String repo) {
    final Try<GithubRepoDetails> details = GithubRepoClient.builder()
        .readOnlyHttpClient(HTTP_CLIENT)
        .repo(url)
        .build()
        .getDetails();

    assertEquals(username, details.get().getUsername());
    assertEquals(repo, details.get().getRepository());
  }
}
