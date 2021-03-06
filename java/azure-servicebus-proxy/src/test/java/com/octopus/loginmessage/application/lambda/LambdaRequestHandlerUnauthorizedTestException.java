package com.octopus.loginmessage.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.loginmessage.BaseTest;
import com.octopus.loginmessage.CommercialAzureServiceBusTestProfile;
import com.octopus.loginmessage.application.TestPaths;
import com.octopus.loginmessage.domain.handlers.ResourceHandler;
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
@TestProfile(CommercialAzureServiceBusTestProfile.class)
public class LambdaRequestHandlerUnauthorizedTestException extends BaseTest {

  @Inject
  LambdaRequestHanlder api;

  @InjectMock
  ResourceHandler handler;

  @BeforeEach
  public void setup() {
    Mockito.doThrow(new UnauthorizedException()).when(handler).create(any(), any(), any(), any(), any());
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
    apiGatewayProxyRequestEvent.setPath(TestPaths.API_ENDPOINT);
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(403, postResponse.getStatusCode());
  }
}
