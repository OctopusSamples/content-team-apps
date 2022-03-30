package com.octopus.loginmessage.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.loginmessage.BaseTest;
import com.octopus.loginmessage.CommercialAzureServiceBusTestProfile;
import com.octopus.loginmessage.application.TestPaths;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.loginmessage.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.loginmessage.domain.entities.GithubUserLoggedInForFreeToolsEventV1Upstream;
import com.octopus.loginmessage.domain.framework.producers.JsonApiConverter;
import com.octopus.loginmessage.infrastructure.octofront.CommercialServiceBus;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.Base64;
import java.util.HashMap;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(CommercialAzureServiceBusTestProfile.class)
public class LambdaRequestHandlerTest extends BaseTest {

  @Inject
  LambdaRequestHanlder api;

  @Inject
  ResourceConverter resourceConverter;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @InjectMock
  CommercialServiceBus commercialServiceBus;

  @Inject
  JsonApiConverter jsonApiConverter;

  @BeforeEach
  public void beforeEach() {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(true);
  }

  @Test
  public void testCreateMessage() throws DocumentSerializationException {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent()
            .withHeaders(
                new HashMap<>() {
                  {
                    put("Accept", "application/vnd.api+json");
                  }
                })
            .withHttpMethod("POST")
            .withPath(TestPaths.API_ENDPOINT)
            .withBody(new String(jsonApiConverter.buildResourceConverter().writeDocument(
                new JSONAPIDocument<>(GithubUserLoggedInForFreeToolsEventV1
                    .builder()
                    .emailAddress("test@test.com")
                    .build()))));
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(202, postResponse.getStatusCode());
  }

  @Test
  public void testCreateMessageBase64Encoded() throws DocumentSerializationException {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent()
            .withHeaders(
                new HashMap<>() {
                  {
                    put("Accept", "application/vnd.api+json");
                  }
                })
            .withHttpMethod("POST")
            .withPath(TestPaths.API_ENDPOINT)
            .withBody(Base64.getEncoder()
                .encodeToString(jsonApiConverter.buildResourceConverter().writeDocument(
                    new JSONAPIDocument<>(GithubUserLoggedInForFreeToolsEventV1
                        .builder()
                        .emailAddress("test@test.com")
                        .build()))))
            .withIsBase64Encoded(true);
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(202, postResponse.getStatusCode());
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
  public void testLambdaCreateWithBadBody() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent()
            .withHeaders(
                new HashMap<>() {
                  {
                    put("Accept", "application/vnd.api+json");
                  }
                })
            .withHttpMethod("POST")
            .withPath(TestPaths.API_ENDPOINT)
            .withBody("Not a valid JSON document");
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(400, postResponse.getStatusCode());
  }

  @Test
  public void testMissingPath() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent()
            .withHeaders(
                new HashMap<>() {
                  {
                    put("Accept", "application/vnd.api+json");
                  }
                })
            .withHttpMethod("GET")
            .withPath("/api/blah");
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(404, postResponse.getStatusCode());
  }

  @Test
  public void testMissingMethod() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent()
            .withHeaders(
                new HashMap<>() {
                  {
                    put("Accept", "application/vnd.api+json");
                  }
                })
            .withHttpMethod("GET")
            .withPath(TestPaths.API_ENDPOINT);
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(404, postResponse.getStatusCode());
  }

  @Test
  public void testHealthCreateItem() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent()
            .withHttpMethod("GET")
            .withPath(TestPaths.HEALTH_ENDPOINT + "/POST");
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(200, postResponse.getStatusCode());
  }
}
