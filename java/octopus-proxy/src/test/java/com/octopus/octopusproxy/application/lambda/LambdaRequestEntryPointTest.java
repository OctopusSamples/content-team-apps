package com.octopus.octopusproxy.application.lambda;

import static org.mockito.ArgumentMatchers.any;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.octopusproxy.domain.handlers.HealthHandler;
import com.octopus.octopusproxy.domain.handlers.ResourceHandler;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LambdaRequestEntryPointTest {

  @Inject
  LambdaRequestEntryPoint lambdaRequestEntryPoint;

  @InjectMock
  ResourceHandler resourceHandler;

  @InjectMock
  HealthHandler healthHandler;

  @BeforeEach
  public void setup() throws DocumentSerializationException {
    Mockito.when(resourceHandler.getAll(any(), any(), any(), any(), any())).thenReturn("");
    Mockito.when(resourceHandler.getOne(any(), any(), any(), any(), any())).thenReturn("");
    Mockito.when(healthHandler.getHealth(any(), any())).thenReturn("");
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/octopusspace", "/api/octopusspace/blah", "/health/octopusspace/x/GET"})
  public void testRequests(final String path) {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath(path);

    Assertions.assertEquals(200,
        lambdaRequestEntryPoint.handleRequest(
            apiGatewayProxyRequestEvent,
            Mockito.mock(Context.class)).getStatusCode());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/nope", "/api/nope/blah"})
  public void testBadRequests(final String path) {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath(path);

    Assertions.assertEquals(404,
        lambdaRequestEntryPoint.handleRequest(
            apiGatewayProxyRequestEvent,
            Mockito.mock(Context.class)).getStatusCode());
  }
}
