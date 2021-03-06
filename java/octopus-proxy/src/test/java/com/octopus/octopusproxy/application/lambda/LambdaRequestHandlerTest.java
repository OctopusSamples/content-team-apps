package com.octopus.octopusproxy.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.encryption.AsymmetricDecryptor;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.octopusproxy.BaseTest;
import com.octopus.octopusproxy.application.Paths;
import com.octopus.octopusproxy.domain.features.ClientPrivateKey;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.HashMap;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LambdaRequestHandlerTest extends BaseTest {

  @Inject
  LambdaRequestEntryPoint api;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @InjectMock
  ClientPrivateKey clientPrivateKey;

  @InjectMock
  AsymmetricDecryptor asymmetricDecryptor;

  @BeforeEach
  public void setup() {
    Mockito.when(asymmetricDecryptor.decrypt(any(), any())).thenReturn("");
    Mockito.when(clientPrivateKey.privateKeyBase64()).thenReturn(Optional.of(""));
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(true);
  }

  @Test
  public void assertEventIsNotNull() {
    assertThrows(NullPointerException.class, () -> {
      api.handleRequest(null, Mockito.mock(Context.class));
    });

    assertThrows(NullPointerException.class, () -> {
      api.handleRequest(new APIGatewayProxyRequestEvent(), null);
    });
  }

  @Test
  public void testMissingPath() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    apiGatewayProxyRequestEvent.setQueryStringParameters(new HashMap<>() {{
      put("apiKey", "blah");
    }});
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath("/api/blah");
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(404, postResponse.getStatusCode());
  }

  @Test
  public void testGetMissingEntity() {
    final APIGatewayProxyRequestEvent getApiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    getApiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put(
                "Accept",
                "application/vnd.api+json");
          }
        });
    getApiGatewayProxyRequestEvent.setQueryStringParameters(new HashMap<>() {{
      put("apiKey", "blah");
    }});
    getApiGatewayProxyRequestEvent.setHttpMethod("GET");
    getApiGatewayProxyRequestEvent.setPath(Paths.API_ENDPOINT + "/10000000000000000000");
    final APIGatewayProxyResponseEvent getResponse =
        api.handleRequest(getApiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(404, getResponse.getStatusCode());
  }
}
