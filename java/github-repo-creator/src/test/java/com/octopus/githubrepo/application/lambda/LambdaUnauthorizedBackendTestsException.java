package com.octopus.githubrepo.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.githubrepo.BaseTest;
import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.handlers.GitHubRepoHandler;
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
public class LambdaUnauthorizedBackendTestsException extends BaseTest {

  private static final String API_ENDPOINT = "/api/populategithubrepo";
  
  @Inject
  ServiceAccountApi api;

  @InjectMock
  GitHubRepoHandler handler;

  @BeforeEach
  public void setup() throws DocumentSerializationException {
    Mockito.when(handler.create(any(), any(), any(), any(), any())).thenThrow(new UnauthorizedException());
  }

  @Test
  public void testUnauthorizedCreate() {
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
    assertEquals(403, postResponse.getStatusCode());
  }
}
