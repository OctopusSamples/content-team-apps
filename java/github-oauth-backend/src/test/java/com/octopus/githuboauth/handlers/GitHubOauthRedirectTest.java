package com.octopus.githuboauth.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import com.octopus.githuboauth.TestingProfile;
import com.octopus.githuboauth.domain.handlers.GitHubOauthRedirect;
import com.octopus.githuboauth.domain.handlers.SimpleResponse;
import com.octopus.githuboauth.domain.oauth.OauthResponse;
import com.octopus.githuboauth.infrastructure.client.GitHubOauth;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Null;

@QuarkusTest
@TestProfile(TestingProfile.class)
public class GitHubOauthRedirectTest {

  @Inject
  GitHubOauthRedirect gitHubOauthRedirect;

  @InjectMock
  @RestClient
  GitHubOauth gitHubOauth;

  @BeforeEach
  public void setup() {
    final OauthResponse oauthResponse = new OauthResponse();
    oauthResponse.setAccessToken("access token");
    Mockito.when(gitHubOauth.accessToken(any(), any(), any(), any())).thenReturn(oauthResponse);
  }

  @Test
  public void testLoginRedirect() {
    final SimpleResponse simpleResponse = gitHubOauthRedirect.oauthRedirect("12345",
        List.of("12345"), "12345");
    assertEquals(303, simpleResponse.getCode(), "The response must be a redirect");
    assertTrue(simpleResponse.getHeaders().keySet().stream().anyMatch("location"::equalsIgnoreCase),
        "The response must set a location");
    assertTrue(simpleResponse.getMultiValueHeaders().keySet().stream()
        .anyMatch("set-cookie"::equalsIgnoreCase), "The response must set cookies");
    assertTrue(simpleResponse.getMultiValueHeaders().keySet()
            .stream()
            .filter("set-cookie"::equalsIgnoreCase)
            .flatMap(k -> simpleResponse.getMultiValueHeaders().get(k).stream())
            .anyMatch(v -> v.startsWith("GitHubUserSession")),
        "The response must include a cookie called GitHubUserSession");
    assertTrue(simpleResponse.getMultiValueHeaders().keySet()
            .stream()
            .filter("set-cookie"::equalsIgnoreCase)
            .flatMap(k -> simpleResponse.getMultiValueHeaders().get(k).stream())
            .anyMatch(v -> v.startsWith("GitHubState")),
        "The response must include a cookie called GitHubState");
  }

  @Test
  public void testLoginRedirectWithInvalidCodes() {
    final SimpleResponse simpleResponse = gitHubOauthRedirect.oauthRedirect("12345",
        List.of("54321"), "12345");
    assertEquals(400, simpleResponse.getCode(), "The response must be a redirect");
  }

  @Test
  public void testLoginRedirectWithNullParams() {
    assertThrows(NullPointerException.class, () -> gitHubOauthRedirect.oauthRedirect(
        null,
        List.of("54321"),
        "12345"));

    assertThrows(NullPointerException.class, () -> gitHubOauthRedirect.oauthRedirect(
        "12345",
        null,
        "12345"));

    assertThrows(NullPointerException.class, () -> gitHubOauthRedirect.oauthRedirect(
        "12345",
        List.of("54321"),
        null));
  }
}
