package com.octopus.octopusproxy.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.octopusproxy.BaseTest;
import com.octopus.octopusproxy.application.Paths;
import com.octopus.octopusproxy.domain.features.ClientPrivateKey;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * These tests verify the health endpoints access via a lambda.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LambdaRequestHandlerHealthTest extends BaseTest {

  @Inject
  LambdaRequestEntryPoint api;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @InjectMock
  ClientPrivateKey clientPrivateKey;

  @BeforeEach
  public void setup() throws DocumentSerializationException {
    Mockito.when(clientPrivateKey.privateKeyBase64()).thenReturn(Optional.of(""));
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(true);
  }

  @Test
  public void testHealthGetItem() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath(Paths.HEALTH_ENDPOINT + "/x/GET");
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(200, postResponse.getStatusCode());
  }
}
