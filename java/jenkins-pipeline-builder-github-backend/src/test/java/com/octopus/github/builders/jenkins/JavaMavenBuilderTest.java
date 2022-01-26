package com.octopus.github.builders.jenkins;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.builders.java.JavaMavenBuilder;
import com.octopus.http.ReadOnlyStringReadOnlyHttpClient;
import com.octopus.repoclients.GithubRepoClient;
import org.junit.jupiter.api.Test;

public class JavaMavenBuilderTest {

  private static final JavaMavenBuilder JAVA_MAVEN_BUILDER = new JavaMavenBuilder();

  @Test
  public void verifyBuilderSupport() {
    assertFalse(JAVA_MAVEN_BUILDER.canBuild(GithubRepoClient
        .builder()
        .readOnlyHttpClient(new ReadOnlyStringReadOnlyHttpClient())
        .repo("https://github.com/OctopusSamples/RandomQuotes")
        .username(System.getenv("APP_GITHUB_ID"))
        .password(System.getenv("APP_GITHUB_SECRET"))
        .build()));

    assertTrue(JAVA_MAVEN_BUILDER.canBuild(GithubRepoClient
        .builder()
        .readOnlyHttpClient(new ReadOnlyStringReadOnlyHttpClient())
        .repo("https://github.com/mcasperson/SampleMavenProject-SpringBoot")
        .username(System.getenv("APP_GITHUB_ID"))
        .password(System.getenv("APP_GITHUB_SECRET"))
        .build()));
  }
}
