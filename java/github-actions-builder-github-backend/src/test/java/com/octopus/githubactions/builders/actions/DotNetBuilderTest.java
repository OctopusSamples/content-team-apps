package com.octopus.githubactions.builders.actions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.githubactions.shared.builders.DotNetCoreBuilder;
import com.octopus.http.impl.ReadOnlyHttpClientImpl;
import com.octopus.repoclients.impl.GithubRepoClient;
import io.vavr.control.Try;
import org.junit.jupiter.api.Test;

public class DotNetBuilderTest {

  private static final DotNetCoreBuilder DOT_NET_CORE_BUILDER = new DotNetCoreBuilder();

  @Test
  public void verifyBuilderSupport() {
    Try.run(() -> Thread.sleep(3000));
    assertFalse(DOT_NET_CORE_BUILDER.canBuild(GithubRepoClient
        .builder()
        .readOnlyHttpClient(new ReadOnlyHttpClientImpl())
        .repo("https://github.com/OctopusSamples/RandomQuotes-Java")
        .username(System.getenv("APP_GITHUB_ID"))
        .password(System.getenv("APP_GITHUB_SECRET"))
        .build()));

    Try.run(() -> Thread.sleep(3000));
    assertTrue(DOT_NET_CORE_BUILDER.canBuild(GithubRepoClient
        .builder()
        .readOnlyHttpClient(new ReadOnlyHttpClientImpl())
        .repo("https://github.com/OctopusSamples/RandomQuotes")
        .username(System.getenv("APP_GITHUB_ID"))
        .password(System.getenv("APP_GITHUB_SECRET"))
        .build()));
  }
}
