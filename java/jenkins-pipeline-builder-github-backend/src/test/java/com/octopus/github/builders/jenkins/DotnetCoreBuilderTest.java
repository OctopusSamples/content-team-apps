package com.octopus.github.builders.jenkins;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.builders.dotnet.DotnetCoreBuilder;
import com.octopus.http.impl.ReadOnlyHttpClientImpl;
import com.octopus.repoclients.impl.GithubRepoClient;
import io.vavr.control.Try;
import org.junit.jupiter.api.Test;

public class DotnetCoreBuilderTest {

  private static final DotnetCoreBuilder DOTNET_CORE_BUILDER = new DotnetCoreBuilder();

  @Test
  public void verifyBuilderSupport() {
    Try.run(() -> Thread.sleep(3000));
    assertTrue(DOTNET_CORE_BUILDER.canBuild(GithubRepoClient
        .builder()
        .readOnlyHttpClient(new ReadOnlyHttpClientImpl())
        .repo("https://github.com/OctopusSamples/RandomQuotes")
        .username(System.getenv("APP_GITHUB_ID"))
        .password(System.getenv("APP_GITHUB_SECRET"))
        .build()));

    Try.run(() -> Thread.sleep(3000));
    assertFalse(DOTNET_CORE_BUILDER.canBuild(GithubRepoClient
        .builder()
        .readOnlyHttpClient(new ReadOnlyHttpClientImpl())
        .repo("https://github.com/mcasperson/SampleGradleProject-SpringBoot")
        .username(System.getenv("APP_GITHUB_ID"))
        .password(System.getenv("APP_GITHUB_SECRET"))
        .build()));
  }
}
