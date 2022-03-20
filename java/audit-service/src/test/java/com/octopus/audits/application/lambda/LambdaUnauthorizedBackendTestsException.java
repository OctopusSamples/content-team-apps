package com.octopus.audits.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.audits.BaseTest;
import com.octopus.audits.domain.exceptions.Unauthorized;
import com.octopus.audits.domain.handlers.AuditsHandler;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.HashMap;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LambdaUnauthorizedBackendTestsException extends BaseTest {

  @Inject
  AuditApi auditApi;

  @InjectMock
  AuditsHandler auditsHandler;

  @BeforeEach
  public void setup() throws DocumentSerializationException {
    Mockito.when(auditsHandler.getOne(any(), any(), any(), any())).thenThrow(new Unauthorized());
    Mockito.when(auditsHandler.getAll(any(), any(), any(), any(), any(), any())).thenThrow(new Unauthorized());
    Mockito.when(auditsHandler.create(any(), any(), any(), any())).thenThrow(new Unauthorized());
  }

  @Test
  public void testUnauthorizedGetAll() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath("/api/audits");
    final ProxyResponse postResponse =
        auditApi.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals("403", postResponse.statusCode);
  }

  @Test
  public void testUnauthorizedGetOne() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath("/api/audits/1");
    final ProxyResponse postResponse =
        auditApi.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals("403", postResponse.statusCode);
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
    apiGatewayProxyRequestEvent.setPath("/api/audits");
    final ProxyResponse postResponse =
        auditApi.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals("403", postResponse.statusCode);
  }
}
