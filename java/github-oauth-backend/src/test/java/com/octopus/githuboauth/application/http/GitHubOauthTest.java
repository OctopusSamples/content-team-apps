package com.octopus.githuboauth.application.http;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.githuboauth.TestingProfile;
import com.octopus.githuboauth.application.TestPaths;
import com.octopus.githuboauth.domain.oauth.OauthResponse;
import com.octopus.githuboauth.infrastructure.client.GitHubOauth;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestProfile(TestingProfile.class)
public class GitHubOauthTest {

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
  public void testLogin()  {
    given()
        .when()
        .redirects().follow(false)
        .get(TestPaths.LOGIN_ENDPOINT)
        .then()
        .statusCode(307);
  }

  @Test
  public void testCodeExchange() {
    given()
        .queryParam("state", "state")
        .queryParam("code", "code")
        .cookie("GitHubState", "state")
        .when()
        .redirects().follow(false)
        .get(TestPaths.CODE_EXCHANGE_ENDPOINT)
        .then()
        .statusCode(303);
  }
}
