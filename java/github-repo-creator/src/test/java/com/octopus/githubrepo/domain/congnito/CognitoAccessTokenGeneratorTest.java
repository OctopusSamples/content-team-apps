package com.octopus.githubrepo.domain.congnito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.cognito.CognitoAccessTokenGenerator;
import com.octopus.githubrepo.infrastructure.clients.CognitoClient;
import com.octopus.oauth.Oauth;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.vavr.control.Try;
import java.util.Date;
import javax.inject.Inject;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(Lifecycle.PER_METHOD)
@TestProfile(TestingProfile.class)
public class CognitoAccessTokenGeneratorTest {

  @InjectMock
  @RestClient
  CognitoClient cognitoClient;

  @Inject
  CognitoAccessTokenGenerator cognitoAccessTokenGenerator;

  @BeforeEach
  public void setup() {
    Mockito.when(cognitoClient.getToken(any(), any(), any(), any())).thenReturn(
        Oauth.builder()
            // return a random string to simulate unguessable access tokens
            .accessToken(RandomStringUtils.random(10, true, true))
            // expire in 60 minutes
            .expiresIn((int) new Date().getTime() / 1000 + 60 * 60)
            .build());
  }

  @Test
  public void getAccessTokenTest() {
    final Try<String> accessToken = cognitoAccessTokenGenerator.getAccessToken();
    final Try<String> accessToken2 = cognitoAccessTokenGenerator.getAccessToken();
    assertTrue(accessToken.isSuccess());
    assertTrue(accessToken2.isSuccess());
    // The first access token should be reused
    assertEquals(accessToken.get(), accessToken.get());
  }
}
