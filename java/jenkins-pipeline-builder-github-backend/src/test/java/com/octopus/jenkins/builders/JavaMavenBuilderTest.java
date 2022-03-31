package com.octopus.jenkins.builders;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.jenkins.shared.builders.java.JavaMavenBuilder;
import com.octopus.http.impl.ReadOnlyHttpClientImpl;
import com.octopus.repoclients.impl.GithubRepoClient;
import io.vavr.control.Try;
import org.junit.jupiter.api.Test;

public class JavaMavenBuilderTest {

  private static final JavaMavenBuilder JAVA_MAVEN_BUILDER = new JavaMavenBuilder();

  @Test
  public void verifyBuilderSupport() {
    Try.run(() -> Thread.sleep(3000));
    assertFalse(JAVA_MAVEN_BUILDER.canBuild(GithubRepoClient
        .builder()
        .readOnlyHttpClient(new ReadOnlyHttpClientImpl())
        .repo("https://github.com/OctopusSamples/RandomQuotes")
        .username(System.getenv("APP_GITHUB_ID"))
        .password(System.getenv("APP_GITHUB_SECRET"))
        .build()));

    Try.run(() -> Thread.sleep(3000));
    assertTrue(JAVA_MAVEN_BUILDER.canBuild(GithubRepoClient
        .builder()
        .readOnlyHttpClient(new ReadOnlyHttpClientImpl())
        .repo("https://github.com/mcasperson/SampleMavenProject-SpringBoot")
        .username(System.getenv("APP_GITHUB_ID"))
        .password(System.getenv("APP_GITHUB_SECRET"))
        .build()));
  }
}
