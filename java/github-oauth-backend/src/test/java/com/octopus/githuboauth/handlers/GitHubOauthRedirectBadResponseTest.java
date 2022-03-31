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

@QuarkusTest
@TestProfile(TestingProfile.class)
public class GitHubOauthRedirectBadResponseTest {

  @Inject
  GitHubOauthRedirect gitHubOauthRedirect;

  @InjectMock
  @RestClient
  GitHubOauth gitHubOauth;

  @BeforeEach
  public void setup() {
    final OauthResponse oauthResponse = new OauthResponse();
    oauthResponse.setError("error");
    Mockito.when(gitHubOauth.accessToken(any(), any(), any(), any())).thenReturn(oauthResponse);
  }

  @Test
  public void testLoginRedirect() {
    final SimpleResponse simpleResponse = gitHubOauthRedirect.oauthRedirect("12345",
        List.of("12345"), "12345");
    assertEquals(500, simpleResponse.getCode(), "The response must be a server error code");
  }
}
