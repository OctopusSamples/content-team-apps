package com.octopus.githubproxy.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.githubproxy.TestingProfile;
import com.octopus.githubproxy.application.Paths;
import com.octopus.githubproxy.domain.handlers.HealthHandler;
import com.octopus.githubproxy.domain.handlers.ResourceHandler;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.HashMap;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * These tests verify the behaviour of the system when the underlying handler throws an exception.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class LambdaRequestHandlerBackendFailureTest {

  @Inject
  LambdaRequestHanlder api;

  @InjectMock
  ResourceHandler handler;

  @InjectMock
  HealthHandler healthHandler;

  @BeforeEach
  public void setup() throws DocumentSerializationException {
    Mockito.when(handler.getOne(any(), any(), any(), any(), any())).thenThrow(new RuntimeException());
    Mockito.when(healthHandler.getHealth(any(), any())).thenThrow(new RuntimeException());
  }


  @Test
  public void testUnexpectedExceptionGetOne() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath(Paths.API_ENDPOINT + "/1");
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
    apiGatewayProxyRequestEvent.setPath(Paths.HEALTH_ENDPOINT + "/GET");
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(500, postResponse.getStatusCode());
  }
}
