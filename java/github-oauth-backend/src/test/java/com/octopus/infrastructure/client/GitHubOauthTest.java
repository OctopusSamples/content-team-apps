package com.octopus.infrastructure.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.githuboauth.domain.oauth.OauthResponse;
import com.octopus.githuboauth.infrastructure.client.GitHubOauth;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class GitHubOauthTest {
  @RestClient
  GitHubOauth gitHubOauth;

  @ConfigProperty(name = "github.client.id")
  String clientId;

  @ConfigProperty(name = "github.client.secret")
  String clientSecret;

  /**
   * This test verifies that the rest client can parse the response correctly. The response
   * will be an error, but that is expected given the bad code being sent.
   */
  @Test
  public void testOauth() {
    final OauthResponse response = gitHubOauth.accessToken(
        clientId,
        clientSecret,
        "abadcode",
        "https://development.githubactionworkflows.com/");
    assertTrue(response.getError().length() != 0);
    assertTrue(response.getErrorDescription().length() != 0);
  }
}
