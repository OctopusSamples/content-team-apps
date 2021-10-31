package com.octopus.repoclients.github;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.http.HttpClient;
import com.octopus.http.StringHttpClient;
import io.vavr.control.Try;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


public class GithubRepoClientTest {

  private static final HttpClient HTTP_CLIENT = new StringHttpClient();

  @Test
  public void testRepoScanning() {
    final Try<List<String>> files = GithubRepoClient.builder()
        .httpClient(HTTP_CLIENT)
        .repo("https://github.com/OctopusSamples/RandomQuotes")
        .build()
        .getWildcardFiles("*.sln");

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
        .httpClient(HTTP_CLIENT)
        .repo(url)
        .build()
        .getDetails();

    assertEquals(username, details.get().getUsername());
    assertEquals(repo, details.get().getRepository());
  }
}
