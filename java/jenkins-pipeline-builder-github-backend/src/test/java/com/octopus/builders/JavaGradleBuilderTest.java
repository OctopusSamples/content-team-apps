package com.octopus.builders;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.builders.java.JavaGradleBuilder;
import com.octopus.http.StringHttpClient;
import com.octopus.repoclients.github.GithubRepoClient;
import org.junit.jupiter.api.Test;

public class JavaGradleBuilderTest {

  private static final JavaGradleBuilder JAVA_GRADLE_BUILDER = new JavaGradleBuilder();

  @Test
  public void verifyBuilderSupport() {
    assertFalse(JAVA_GRADLE_BUILDER.canBuild(GithubRepoClient
        .builder()
        .httpClient(new StringHttpClient())
        .repo("https://github.com/OctopusSamples/RandomQuotes")
        .username(System.getenv("APP_GITHUB_ID"))
        .password(System.getenv("APP_GITHUB_SECRET"))
        .build()));

    assertTrue(JAVA_GRADLE_BUILDER.canBuild(GithubRepoClient
        .builder()
        .httpClient(new StringHttpClient())
        .repo("https://github.com/mcasperson/SampleGradleProject-SpringBoot")
        .username(System.getenv("APP_GITHUB_ID"))
        .password(System.getenv("APP_GITHUB_SECRET"))
        .build()));
  }
}
