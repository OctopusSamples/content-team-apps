package com.octopus.audits.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.audits.BaseTest;
import com.octopus.audits.domain.entities.Audit;
import com.octopus.audits.domain.handlers.AuditsHandler;
import com.octopus.audits.domain.handlers.HealthHandler;
import com.octopus.audits.infrastructure.utilities.LiquidbaseUpdater;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LambdaBadBackendTests extends BaseTest {

  @Inject
  AuditApi auditApi;

  @InjectMock
  AuditsHandler auditsHandler;

  @InjectMock
  HealthHandler healthHandler;

  @BeforeEach
  public void setup() throws DocumentSerializationException {
    Mockito.when(auditsHandler.getOne(any(), any(), any(), any())).thenThrow(new RuntimeException());
    Mockito.when(auditsHandler.getAll(any(), any(), any(), any(), any(), any())).thenThrow(new RuntimeException());
    Mockito.when(auditsHandler.create(any(), any(), any(), any())).thenThrow(new RuntimeException());
    Mockito.when(healthHandler.getHealth(any(), any())).thenThrow(new RuntimeException());
  }

  @Test
  public void testUnexpectedExceptionGetAll() {
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
    assertEquals("500", postResponse.statusCode);
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
    apiGatewayProxyRequestEvent.setPath("/api/audits/1");
    final ProxyResponse postResponse =
        auditApi.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals("500", postResponse.statusCode);
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
    apiGatewayProxyRequestEvent.setPath("/api/audits");
    final ProxyResponse postResponse =
        auditApi.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals("500", postResponse.statusCode);
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
    apiGatewayProxyRequestEvent.setPath("/health/audits/GET");
    final ProxyResponse postResponse =
        auditApi.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals("500", postResponse.statusCode);
  }

}
