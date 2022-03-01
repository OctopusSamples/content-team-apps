package com.octopus.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octopus.lambda.ProxyResponseBuilder;
import com.octopus.lambda.impl.ProxyResponseBuilderImpl;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ProxyResponseBuilderTest {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final ProxyResponseBuilder PROXY_RESPONSE_BUILDER = new ProxyResponseBuilderImpl();

  @Test
  public void testNulls() {
    assertThrows(NullPointerException.class, () -> {
      PROXY_RESPONSE_BUILDER.buildError(null);
    });

    assertThrows(NullPointerException.class, () -> {
      PROXY_RESPONSE_BUILDER.buildError(new Exception(), null);
    });

    assertThrows(NullPointerException.class, () -> {
      PROXY_RESPONSE_BUILDER.buildError(null, "");
    });

    assertThrows(NullPointerException.class, () -> {
      PROXY_RESPONSE_BUILDER.buildUnauthorizedRequest(null);
    });

    assertThrows(NullPointerException.class, () -> {
      PROXY_RESPONSE_BUILDER.buildBadRequest(null);
    });
  }

  @Test
  public void testBuildError() throws JsonProcessingException {
    final APIGatewayProxyResponseEvent response = PROXY_RESPONSE_BUILDER.buildError(new Exception());
    final Map<String, Object> parsedBody = OBJECT_MAPPER.readValue(response.getBody(), Map.class);
    assertTrue(parsedBody.containsKey("errors"));
    assertFalse(((List)parsedBody.get("errors")).isEmpty());
    assertTrue(((Map)((List)parsedBody.get("errors")).get(0)).containsKey("code"));
    assertEquals(500, response.getStatusCode());
  }

  @Test
  public void testBuildErrorWithBody() throws JsonProcessingException {
    final APIGatewayProxyResponseEvent response = PROXY_RESPONSE_BUILDER.buildError(new Exception(), "the call body");
    final Map<String, Object> parsedBody = OBJECT_MAPPER.readValue(response.getBody(), Map.class);
    assertTrue(parsedBody.containsKey("errors"));
    assertFalse(((List)parsedBody.get("errors")).isEmpty());
    assertTrue(((Map)((List)parsedBody.get("errors")).get(0)).containsKey("code"));
    assertTrue(((Map)((List)parsedBody.get("errors")).get(0)).containsKey("meta"));
    assertTrue(((Map)((Map)((List)parsedBody.get("errors")).get(0)).get("meta")).containsKey("requestBody"));
    assertEquals("the call body", ((Map)((Map)((List)parsedBody.get("errors")).get(0)).get("meta")).get("requestBody"));
    assertEquals(500, response.getStatusCode());
  }

  @Test
  public void testBuildNotFound() {
    final APIGatewayProxyResponseEvent response = PROXY_RESPONSE_BUILDER.buildNotFound();
    assertEquals(404, response.getStatusCode());
  }

  @Test
  public void testBuildUnauthorized() throws JsonProcessingException {
    final APIGatewayProxyResponseEvent response = PROXY_RESPONSE_BUILDER.buildUnauthorizedRequest(new Exception());
    final Map<String, Object> parsedBody = OBJECT_MAPPER.readValue(response.getBody(), Map.class);
    assertTrue(parsedBody.containsKey("errors"));
    assertFalse(((List)parsedBody.get("errors")).isEmpty());
    assertTrue(((Map)((List)parsedBody.get("errors")).get(0)).containsKey("title"));
    assertEquals(403, response.getStatusCode());
  }

  @Test
  public void testBuildBadRequest() throws JsonProcessingException {
    final APIGatewayProxyResponseEvent response = PROXY_RESPONSE_BUILDER.buildBadRequest(new Exception());
    final Map<String, Object> parsedBody = OBJECT_MAPPER.readValue(response.getBody(), Map.class);
    assertTrue(parsedBody.containsKey("errors"));
    assertFalse(((List)parsedBody.get("errors")).isEmpty());
    assertTrue(((Map)((List)parsedBody.get("errors")).get(0)).containsKey("code"));
    assertEquals(400, response.getStatusCode());
  }
}
