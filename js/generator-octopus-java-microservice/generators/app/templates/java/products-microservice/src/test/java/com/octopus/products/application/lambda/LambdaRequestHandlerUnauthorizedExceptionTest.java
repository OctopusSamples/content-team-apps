package com.octopus.products.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.products.BaseTest;
import com.octopus.products.application.Paths;
import com.octopus.products.domain.handlers.ResourceHandlerCreate;
import com.octopus.products.domain.handlers.ResourceHandlerGetAll;
import com.octopus.products.domain.handlers.ResourceHandlerGetOne;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.HashMap;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * These tests verify the behaviour of unauthorized requests.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LambdaRequestHandlerUnauthorizedExceptionTest extends BaseTest {

  @Inject
  LambdaRequestEntryPoint api;

  @InjectMock
  ResourceHandlerGetOne handlerGetOne;

  @InjectMock
  ResourceHandlerGetAll handlerGetAll;

  @InjectMock
  ResourceHandlerCreate handlerCreate;

  @BeforeEach
  public void setup() throws DocumentSerializationException {
    Mockito.when(handlerGetOne.getOne(any(), any(), any(), any())).thenThrow(new UnauthorizedException());
    Mockito.when(handlerGetAll.getAll(any(), any(), any(), any(), any(), any())).thenThrow(new UnauthorizedException());
    Mockito.when(handlerCreate.create(any(), any(), any(), any())).thenThrow(new UnauthorizedException());
  }

  @Test
  public void testUnauthorizedExceptionGetAll() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath(Paths.API_ENDPOINT);
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(403, postResponse.getStatusCode());
  }

  @Test
  public void testUnauthorizedExceptionGetOne() {
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
    assertEquals(403, postResponse.getStatusCode());
  }

  @Test
  public void testUnauthorizedExceptionCreate() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    apiGatewayProxyRequestEvent.setHttpMethod("POST");
    apiGatewayProxyRequestEvent.setPath(Paths.API_ENDPOINT);
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(403, postResponse.getStatusCode());
  }
}
