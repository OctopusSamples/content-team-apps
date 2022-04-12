package com.octopus.githubrepo.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.handlers.GitHubRepoHandler;
import com.octopus.githubrepo.domain.handlers.HealthHandler;
import com.octopus.githubrepo.domain.handlers.populaterepo.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.HashMap;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class LambdaBadBackendTests extends BaseTest {

  private static final String API_ENDPOINT = "/api/populategithubrepo";
  private static final String HEALTH_ENDPOINT = "/health/populategithubrepo";

  @Inject
  GithubRepoApi api;

  @InjectMock
  GitHubRepoHandler handler;

  @InjectMock
  HealthHandler healthHandler;

  @BeforeEach
  public void setup() throws DocumentSerializationException {
    Mockito.when(handler.create(any(), any(), any(), any(), any())).thenThrow(new RuntimeException());
    Mockito.when(healthHandler.getHealth(any(), any())).thenThrow(new RuntimeException());
  }

  @Test
  public void testUnexpectedExceptionCreate() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    apiGatewayProxyRequestEvent.setHttpMethod("POST");
    apiGatewayProxyRequestEvent.setPath(API_ENDPOINT);
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(500, postResponse.getStatusCode());
  }

  @Test
  public void testUnexpectedExceptionHealth() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath(HEALTH_ENDPOINT + "/POST");
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(500, postResponse.getStatusCode());
  }

}
