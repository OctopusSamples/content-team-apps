package com.octopus.githuboauth.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.collect.ImmutableMap;
import com.octopus.githuboauth.OauthRedirectLambdaProfile;
import com.octopus.githuboauth.application.TestPaths;
import com.octopus.githuboauth.domain.oauth.OauthResponse;
import com.octopus.githuboauth.infrastructure.client.GitHubOauth;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestProfile(OauthRedirectLambdaProfile.class)
public class GitHubOauthRedirectLambdaTest {
  @Inject
  GitHubOauthRedirectLambda gitHubOauthRedirectLambda;

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
  public void testCodeExchange() {
    final APIGatewayProxyResponseEvent response = gitHubOauthRedirectLambda.handleRequest(
        new APIGatewayProxyRequestEvent()
            .withPath(TestPaths.CODE_EXCHANGE_ENDPOINT)
            .withQueryStringParameters(new ImmutableMap.Builder<String, String>()
                .put("code", "code")
                .put("state", "state")
                .build())
            .withHeaders(new ImmutableMap.Builder<String, String>()
                .put("Cookie", "GitHubState=state")
                .build()),
        Mockito.mock(Context.class));

    assertEquals(303, response.getStatusCode(), "response must be a redirect");
    assertTrue(response.getHeaders().keySet().stream().anyMatch("location"::equalsIgnoreCase));
  }

  @Test
  public void testNullArgs() {
    assertThrows(NullPointerException.class, () -> gitHubOauthRedirectLambda.handleRequest(
        null,
        Mockito.mock(Context.class)));

    assertThrows(NullPointerException.class, () -> gitHubOauthRedirectLambda.handleRequest(
        new APIGatewayProxyRequestEvent().withPath(TestPaths.LOGIN_ENDPOINT),
        null));
  }
}
