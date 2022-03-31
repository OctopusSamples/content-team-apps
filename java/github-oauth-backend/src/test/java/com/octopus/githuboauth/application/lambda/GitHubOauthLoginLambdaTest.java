package com.octopus.githuboauth.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.octopus.githuboauth.OauthLoginLambaProfile;
import com.octopus.githuboauth.application.TestPaths;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestProfile(OauthLoginLambaProfile.class)
public class GitHubOauthLoginLambdaTest {

  @Inject
  GitHubOauthLoginLambda gitHubOauthLoginLambda;

  @Test
  public void testOauthLogin() {
    final APIGatewayProxyResponseEvent response = gitHubOauthLoginLambda.handleRequest(
        new APIGatewayProxyRequestEvent().withPath(TestPaths.LOGIN_ENDPOINT),
        Mockito.mock(Context.class));

    assertEquals(307, response.getStatusCode(), "Response must be a redirect");
    assertTrue(response.getHeaders().keySet().stream().anyMatch("location"::equalsIgnoreCase),
        "The response must set a location");
    assertTrue(response.getHeaders().keySet().stream()
        .anyMatch("set-cookie"::equalsIgnoreCase), "The response must set cookies");
    assertTrue(response.getHeaders().keySet()
            .stream()
            .filter("set-cookie"::equalsIgnoreCase)
            .map(k -> response.getHeaders().get(k))
            .anyMatch(v -> v.startsWith("GitHubState")),
        "The response must include a cookie called GitHubUserSession");
  }

  @Test
  public void testNullArgs() {
    assertThrows(NullPointerException.class, () -> gitHubOauthLoginLambda.handleRequest(
        null,
        Mockito.mock(Context.class)));

    assertThrows(NullPointerException.class, () -> gitHubOauthLoginLambda.handleRequest(
        new APIGatewayProxyRequestEvent().withPath(TestPaths.LOGIN_ENDPOINT),
        null));
  }
}
