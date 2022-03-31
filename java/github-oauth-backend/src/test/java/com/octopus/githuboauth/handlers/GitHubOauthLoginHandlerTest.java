package com.octopus.githuboauth.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.githuboauth.TestingProfile;
import com.octopus.githuboauth.domain.handlers.GitHubOauthLoginHandler;
import com.octopus.githuboauth.domain.handlers.SimpleResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(TestingProfile.class)
public class GitHubOauthLoginHandlerTest {
  @Inject
  GitHubOauthLoginHandler gitHubOauthLoginHandler;

  @Test
  public void testLogin() {
    final SimpleResponse simpleResponse = gitHubOauthLoginHandler.oauthLoginRedirect();
    assertEquals(307, simpleResponse.getCode(), "The response must be a redirect");
    assertTrue(simpleResponse.getHeaders().keySet().stream().anyMatch("location"::equalsIgnoreCase),
        "The response must set a location");
    assertTrue(simpleResponse.getHeaders().keySet().stream()
        .anyMatch("set-cookie"::equalsIgnoreCase), "The response must set cookies");
    assertTrue(simpleResponse.getHeaders().keySet()
            .stream()
            .filter("set-cookie"::equalsIgnoreCase)
            .map(k -> simpleResponse.getHeaders().get(k))
            .anyMatch(v -> v.startsWith("GitHubState")),
        "The response must include a cookie called GitHubUserSession");
  }
}
