package com.octopus.githubrepo.domain.congnito;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.cognito.CognitoAccessTokenGenerator;
import com.octopus.githubrepo.infrastructure.clients.CognitoClient;
import com.octopus.oauth.Oauth;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.Date;
import javax.inject.Inject;
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
public class CognitoAccessTokenGeneratorFailureTest {

  @InjectMock
  @RestClient
  CognitoClient cognitoClient;

  @Inject
  CognitoAccessTokenGenerator cognitoAccessTokenGenerator;

  @BeforeEach
  public void setup() {
    Mockito.when(cognitoClient.getToken(any(), any(), any(), any())).thenThrow(new RuntimeException());
  }

  @Test
  public void getAccessTokenTest() {
    assertTrue(cognitoAccessTokenGenerator.getAccessToken().isFailure());
  }
}
