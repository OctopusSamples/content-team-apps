package com.octopus.customers.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.customers.BaseTest;
import com.octopus.customers.domain.entities.Customer;
import com.octopus.customers.infrastructure.utilities.LiquidbaseUpdater;
import com.octopus.features.DisableSecurityFeature;
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
public class LambdaTests extends BaseTest {

  private static final String API_ENDPOINT = "/api/customers";
  private static final String HEALTH_ENDPOINT = "/health/customers";

  @Inject
  CustomersApi api;

  @Inject
  LiquidbaseUpdater liquidbaseUpdater;

  @Inject
  ResourceConverter resourceConverter;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @BeforeEach
  public void beforeEach() {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(true);
  }

  @BeforeAll
  public void setup() throws SQLException, LiquibaseException {
    liquidbaseUpdater.update();
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
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    apiGatewayProxyRequestEvent.setHttpMethod("POST");
    apiGatewayProxyRequestEvent.setPath(API_ENDPOINT);
    apiGatewayProxyRequestEvent.setBody("Not a valid JSON document");
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(400, postResponse.getStatusCode());
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
    apiGatewayProxyRequestEvent.setPath(API_ENDPOINT);
    apiGatewayProxyRequestEvent.setBody(
        resourceToResourceDocument(resourceConverter, createResource("testCreateAndGetResource")));
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    final Customer postEntity = getResourceFromDocument(resourceConverter, postResponse.getBody());
    assertEquals("testCreateAndGetResource", postEntity.getFirstName());
    assertEquals(200, postResponse.getStatusCode());

    final APIGatewayProxyRequestEvent getApiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    getApiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    getApiGatewayProxyRequestEvent.setHttpMethod("GET");
    getApiGatewayProxyRequestEvent.setPath(API_ENDPOINT + "/" + postEntity.getId());
    final APIGatewayProxyResponseEvent getResponse =
        api.handleRequest(getApiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    final Customer getEntity = getResourceFromDocument(resourceConverter, getResponse.getBody());
    assertEquals(getEntity.getFirstName(), postEntity.getFirstName());
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
    apiGatewayProxyRequestEvent.setPath(API_ENDPOINT);
    apiGatewayProxyRequestEvent.setBody(
        Base64.getEncoder().encodeToString(
            resourceToResourceDocument(resourceConverter,
                createResource("testCreateAndGetResource")).getBytes()));
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    final Customer postEntity = getResourceFromDocument(resourceConverter, postResponse.getBody());
    assertEquals("testCreateAndGetResource", postEntity.getFirstName());
    assertEquals(200, postResponse.getStatusCode());

    final APIGatewayProxyRequestEvent getApiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    getApiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    getApiGatewayProxyRequestEvent.setHttpMethod("GET");
    getApiGatewayProxyRequestEvent.setPath(API_ENDPOINT + "/" + postEntity.getId());
    final APIGatewayProxyResponseEvent getResponse =
        api.handleRequest(getApiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    final Customer getEntity = getResourceFromDocument(resourceConverter, getResponse.getBody());
    assertEquals(getEntity.getFirstName(), postEntity.getFirstName());
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
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(404, postResponse.getStatusCode());
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
    apiGatewayProxyRequestEvent.setPath(API_ENDPOINT);
    apiGatewayProxyRequestEvent.setBody(
        resourceToResourceDocument(resourceConverter, createResource("testCreateAndGetResource")));
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    final Customer postEntity = getResourceFromDocument(resourceConverter, postResponse.getBody());
    assertEquals("testCreateAndGetResource", postEntity.getFirstName());
    assertEquals(200, postResponse.getStatusCode());

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
      getApiGatewayProxyRequestEvent.setPath(API_ENDPOINT);
      getApiGatewayProxyRequestEvent.setQueryStringParameters(
          new HashMap<>() {
            {
              put("filter", "id==" + postEntity.getId());
            }
          });
      final APIGatewayProxyResponseEvent getResponse =
          api.handleRequest(getApiGatewayProxyRequestEvent, Mockito.mock(Context.class));
      final List<Customer> getEntities =
          getResourcesFromDocument(resourceConverter, getResponse.getBody());
      assertTrue(
          getEntities.stream().anyMatch(p -> p.getFirstName().equals(postEntity.getFirstName())));
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
      getApiGatewayProxyRequestEvent.setPath(API_ENDPOINT);
      getApiGatewayProxyRequestEvent.setQueryStringParameters(
          new HashMap<>() {
            {
              put("filter", "firstName==doesnotexist");
            }
          });
      final APIGatewayProxyResponseEvent getResponse =
          api.handleRequest(getApiGatewayProxyRequestEvent, Mockito.mock(Context.class));
      final List<Customer> getEntities =
          getResourcesFromDocument(resourceConverter, getResponse.getBody());
      assertTrue(getEntities.isEmpty());
    }
  }

  @Test
  public void testLambdaBadFilter() {

    final APIGatewayProxyRequestEvent getApiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    getApiGatewayProxyRequestEvent.setHeaders(
        new HashMap<>() {
          {
            put("Accept", "application/vnd.api+json");
          }
        });
    getApiGatewayProxyRequestEvent.setHttpMethod("GET");
    getApiGatewayProxyRequestEvent.setPath(API_ENDPOINT);
    getApiGatewayProxyRequestEvent.setQueryStringParameters(
        new HashMap<>() {
          {
            put("filter", "(*&^%$&*(^)");
          }
        });
    final APIGatewayProxyResponseEvent getResponse =
        api.handleRequest(getApiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(400, getResponse.getStatusCode());
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
    getApiGatewayProxyRequestEvent.setPath(API_ENDPOINT + "/10000000000000000000");
    final APIGatewayProxyResponseEvent getResponse =
        api.handleRequest(getApiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(404, getResponse.getStatusCode());
  }

  @Test
  public void testHealthCollection() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath(HEALTH_ENDPOINT + "/GET");
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(200, postResponse.getStatusCode());
  }

  @Test
  public void testHealthCreateItem() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath(HEALTH_ENDPOINT + "/POST");
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(200, postResponse.getStatusCode());
  }

  @Test
  public void testHealthGetItem() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent =
        new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath(HEALTH_ENDPOINT + "/x/GET");
    final APIGatewayProxyResponseEvent postResponse =
        api.handleRequest(apiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(200, postResponse.getStatusCode());
  }
}
