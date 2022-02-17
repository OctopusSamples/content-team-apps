package com.octopus.audits.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.audits.BaseTest;
import com.octopus.audits.application.lambda.AuditApi;
import com.octopus.audits.application.lambda.ProxyResponse;
import com.octopus.audits.domain.entities.Audit;
import com.octopus.audits.infrastructure.utilities.LiquidbaseUpdater;
import io.quarkus.test.junit.QuarkusTest;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LambdaTests extends BaseTest {

  @Inject
  AuditApi auditApi;

  @Inject
  LiquidbaseUpdater liquidbaseUpdater;

  @Inject
  ResourceConverter resourceConverter;

  @BeforeAll
  public void setup() throws SQLException, LiquibaseException {
    liquidbaseUpdater.update();
  }

  @Test
  public void assertEventIsNotNull() {
    assertThrows(NullPointerException.class, () -> {
      auditApi.handleRequest(null, Mockito.mock(Context.class));
    });

    assertThrows(NullPointerException.class, () -> {
      auditApi.handleRequest(new APIGatewayProxyRequestEvent(), null);
    });
  }

  @Test
  public void testLambdaCreateWithBadBody() throws DocumentSerializationException {
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
    apiGatewayProxyRequestEvent.setBody("Not a valid JSON document");
    final ProxyResponse postResponse =
        auditApi.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals("400", postResponse.statusCode);
  }

  @Test
  public void testLambdaCreateAndGet() throws DocumentSerializationException {
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
    apiGatewayProxyRequestEvent.setBody(
        auditToResourceDocument(resourceConverter, createAudit("testCreateAndGetResource")));
    final ProxyResponse postResponse =
        auditApi.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    final Audit postEntity = getAuditFromDocument(resourceConverter, postResponse.body);
    assertEquals("testCreateAndGetResource", postEntity.getSubject());
    assertEquals("200", postResponse.statusCode);

    final APIGatewayProxyRequestEvent getApiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    getApiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    getApiGatewayProxyRequestEvent.setHttpMethod("GET");
    getApiGatewayProxyRequestEvent.setPath("/api/audits/" + postEntity.getId());
    final ProxyResponse getResponse =
        auditApi.handleRequest(getApiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    final Audit getEntity = getAuditFromDocument(resourceConverter, getResponse.body);
    assertEquals(getEntity.getSubject(), postEntity.getSubject());
  }

  @Test
  public void testLambdaCreateAndGetBase64Encoded() throws DocumentSerializationException {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setIsBase64Encoded(true);
    apiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    apiGatewayProxyRequestEvent.setHttpMethod("POST");
    apiGatewayProxyRequestEvent.setPath("/api/audits");
    apiGatewayProxyRequestEvent.setBody(
        Base64.getEncoder().encodeToString(
            auditToResourceDocument(resourceConverter,
                createAudit("testCreateAndGetResource")).getBytes()));
    final ProxyResponse postResponse =
        auditApi.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    final Audit postEntity = getAuditFromDocument(resourceConverter, postResponse.body);
    assertEquals("testCreateAndGetResource", postEntity.getSubject());
    assertEquals("200", postResponse.statusCode);

    final APIGatewayProxyRequestEvent getApiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    getApiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    getApiGatewayProxyRequestEvent.setHttpMethod("GET");
    getApiGatewayProxyRequestEvent.setPath("/api/audits/" + postEntity.getId());
    final ProxyResponse getResponse =
        auditApi.handleRequest(getApiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    final Audit getEntity = getAuditFromDocument(resourceConverter, getResponse.body);
    assertEquals(getEntity.getSubject(), postEntity.getSubject());
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
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath("/api/blah");
    final ProxyResponse postResponse =
        auditApi.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals("404", postResponse.statusCode);
  }

  @Test
  public void testLambdaCreateAndGetFilter() throws DocumentSerializationException {
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
    apiGatewayProxyRequestEvent.setBody(
        auditToResourceDocument(resourceConverter, createAudit("testCreateAndGetResource")));
    final ProxyResponse postResponse =
        auditApi.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    final Audit postEntity = getAuditFromDocument(resourceConverter, postResponse.body);
    assertEquals("testCreateAndGetResource", postEntity.getSubject());
    assertEquals("200", postResponse.statusCode);

    {
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
      getApiGatewayProxyRequestEvent.setHttpMethod("GET");
      getApiGatewayProxyRequestEvent.setPath("/api/audits");
      getApiGatewayProxyRequestEvent.setQueryStringParameters(
          new HashMap<>() {
            {
              put("filter", "id==" + postEntity.getId());
            }
          });
      final ProxyResponse getResponse =
          auditApi.handleRequest(getApiGatewayProxyRequestEvent, Mockito.mock(Context.class));
      final List<Audit> getEntities =
          getAuditsFromDocument(resourceConverter, getResponse.body);
      assertTrue(
          getEntities.stream().anyMatch(p -> p.getSubject().equals(postEntity.getSubject())));
    }

    {
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
      getApiGatewayProxyRequestEvent.setHttpMethod("GET");
      getApiGatewayProxyRequestEvent.setPath("/api/audits");
      getApiGatewayProxyRequestEvent.setQueryStringParameters(
          new HashMap<>() {
            {
              put("filter", "subject==doesnotexist");
            }
          });
      final ProxyResponse getResponse =
          auditApi.handleRequest(getApiGatewayProxyRequestEvent, Mockito.mock(Context.class));
      final List<Audit> getEntities =
          getAuditsFromDocument(resourceConverter, getResponse.body);
      assertTrue(getEntities.isEmpty());
    }
  }

  @Test
  public void testLambdaBadFilter() throws DocumentSerializationException {

    final APIGatewayProxyRequestEvent getApiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    getApiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    getApiGatewayProxyRequestEvent.setHttpMethod("GET");
    getApiGatewayProxyRequestEvent.setPath("/api/audits");
    getApiGatewayProxyRequestEvent.setQueryStringParameters(
        new HashMap<>() {
          {
            put("filter", "(*&^%$&*(^)");
          }
        });
    final ProxyResponse getResponse =
        auditApi.handleRequest(getApiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals("400", getResponse.statusCode);
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
    getApiGatewayProxyRequestEvent.setHttpMethod("GET");
    getApiGatewayProxyRequestEvent.setPath("/api/audits/10000000000000000000");
    final ProxyResponse getResponse =
        auditApi.handleRequest(getApiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals("404", getResponse.statusCode);
  }

  @Test
  public void testHealthCollection() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath("/health/audits/GET");
    final ProxyResponse postResponse =
        auditApi.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals("200", postResponse.statusCode);
  }

  @Test
  public void testHealthCreateItem() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath("/health/audits/POST");
    final ProxyResponse postResponse =
        auditApi.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals("200", postResponse.statusCode);
  }

  @Test
  public void testHealthGetItem() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath("/health/audits/x/GET");
    final ProxyResponse postResponse =
        auditApi.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals("200", postResponse.statusCode);
  }
}
