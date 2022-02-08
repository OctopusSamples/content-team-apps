package com.octopus.github.builders.jenkins;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.builders.java.JavaGradleBuilder;
import com.octopus.http.ReadOnlyStringReadOnlyHttpClient;
import com.octopus.repoclients.impl.GithubRepoClient;
import io.vavr.control.Try;
import org.junit.jupiter.api.Test;

public class JavaGradleBuilderTest {

  private static final JavaGradleBuilder JAVA_GRADLE_BUILDER = new JavaGradleBuilder();

  @Test
  public void verifyBuilderSupport() {
    Try.run(() -> Thread.sleep(3000));
    assertFalse(JAVA_GRADLE_BUILDER.canBuild(GithubRepoClient
        .builder()
        .readOnlyHttpClient(new ReadOnlyStringReadOnlyHttpClient())
        .repo("https://github.com/OctopusSamples/RandomQuotes")
        .username(System.getenv("APP_GITHUB_ID"))
        .password(System.getenv("APP_GITHUB_SECRET"))
        .build()));

    Try.run(() -> Thread.sleep(3000));
    assertTrue(JAVA_GRADLE_BUILDER.canBuild(GithubRepoClient
        .builder()
        .readOnlyHttpClient(new ReadOnlyStringReadOnlyHttpClient())
        .repo("https://github.com/mcasperson/SampleGradleProject-SpringBoot")
        .username(System.getenv("APP_GITHUB_ID"))
        .password(System.getenv("APP_GITHUB_SECRET"))
        .build()));
  }
}
