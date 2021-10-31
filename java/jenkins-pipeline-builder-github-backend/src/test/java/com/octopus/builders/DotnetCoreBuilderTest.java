package com.octopus.builders;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.builders.dotnet.DotnetCoreBuilder;
import com.octopus.http.StringHttpClient;
import com.octopus.repoclients.github.GithubRepoClient;
import org.junit.jupiter.api.Test;

public class DotnetCoreBuilderTest {

  private static final DotnetCoreBuilder DOTNET_CORE_BUILDER = new DotnetCoreBuilder();

  @Test
  public void verifyBuilderSupport() {
    assertTrue(DOTNET_CORE_BUILDER.canBuild(GithubRepoClient
        .builder()
        .httpClient(new StringHttpClient())
        .repo("https://github.com/OctopusSamples/RandomQuotes")
        .username(System.getenv("APP_GITHUB_ID"))
        .password(System.getenv("APP_GITHUB_SECRET"))
        .build()));

    assertFalse(DOTNET_CORE_BUILDER.canBuild(GithubRepoClient
        .builder()
        .httpClient(new StringHttpClient())
        .repo("https://github.com/mcasperson/SampleGradleProject-SpringBoot")
        .username(System.getenv("APP_GITHUB_ID"))
        .password(System.getenv("APP_GITHUB_SECRET"))
        .build()));
  }
}
