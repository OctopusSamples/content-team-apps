package com.octopus.githubactions.github.infrastructure.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.octopus.githubactions.github.domain.entities.GitHubEmail;
import com.octopus.githubactions.github.domain.framework.WireMockExtensions;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

/**
 * Verify the round trip accessing the GitHub API mocked with WireMock. This is mostly used to
 * verify serialization with Lombok based data objects, which can interfere with Jackson if you have
 * not set the @Jacksonized annotation.
 */
@QuarkusTest
@QuarkusTestResource(WireMockExtensions.class)
public class GitHubApiTest {

  @RestClient
  GitHubApi gitHubApi;

  @Test
  public void testPublicEmails() {
    final GitHubEmail[] emails = gitHubApi.publicEmails("whatever");
    assertNotNull(emails);
    assertEquals(1, emails.length);
    assertEquals("matthewcasperson@example.org", emails[0].getEmail());
  }
}
