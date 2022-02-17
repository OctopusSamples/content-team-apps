package com.octopus.audits.domain.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octopus.audits.application.lambda.ProxyResponse;
import com.octopus.audits.domain.utilities.ProxyResponseBuilder;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ProxyResponseBuilderTest {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  public void testNulls() {
    assertThrows(NullPointerException.class, () -> {
      ProxyResponseBuilder.buildError(null);
    });

    assertThrows(NullPointerException.class, () -> {
      ProxyResponseBuilder.buildError(new Exception(), null);
    });

    assertThrows(NullPointerException.class, () -> {
      ProxyResponseBuilder.buildError(null, "");
    });

    assertThrows(NullPointerException.class, () -> {
      ProxyResponseBuilder.buildUnauthorizedRequest(null);
    });

    assertThrows(NullPointerException.class, () -> {
      ProxyResponseBuilder.buildBadRequest(null);
    });
  }

  @Test
  public void testBuildError() throws JsonProcessingException {
    final ProxyResponse response = ProxyResponseBuilder.buildError(new Exception());
    final Map<String, Object> parsedBody = OBJECT_MAPPER.readValue(response.body, Map.class);
    assertTrue(parsedBody.containsKey("errors"));
    assertFalse(((List)parsedBody.get("errors")).isEmpty());
    assertTrue(((Map)((List)parsedBody.get("errors")).get(0)).containsKey("code"));
    assertEquals("500", response.statusCode);
  }

  @Test
  public void testBuildErrorWithBody() throws JsonProcessingException {
    final ProxyResponse response = ProxyResponseBuilder.buildError(new Exception(), "the call body");
    final Map<String, Object> parsedBody = OBJECT_MAPPER.readValue(response.body, Map.class);
    assertTrue(parsedBody.containsKey("errors"));
    assertFalse(((List)parsedBody.get("errors")).isEmpty());
    assertTrue(((Map)((List)parsedBody.get("errors")).get(0)).containsKey("code"));
    assertTrue(((Map)((List)parsedBody.get("errors")).get(0)).containsKey("meta"));
    assertTrue(((Map)((Map)((List)parsedBody.get("errors")).get(0)).get("meta")).containsKey("requestBody"));
    assertEquals("the call body", ((Map)((Map)((List)parsedBody.get("errors")).get(0)).get("meta")).get("requestBody"));
    assertEquals("500", response.statusCode);
  }

  @Test
  public void testBuildNotFound() {
    final ProxyResponse response = ProxyResponseBuilder.buildNotFound();
    assertEquals("404", response.statusCode);
  }

  @Test
  public void testBuildUnauthorized() throws JsonProcessingException {
    final ProxyResponse response = ProxyResponseBuilder.buildUnauthorizedRequest(new Exception());
    final Map<String, Object> parsedBody = OBJECT_MAPPER.readValue(response.body, Map.class);
    assertTrue(parsedBody.containsKey("errors"));
    assertFalse(((List)parsedBody.get("errors")).isEmpty());
    assertTrue(((Map)((List)parsedBody.get("errors")).get(0)).containsKey("title"));
    assertEquals("403", response.statusCode);
  }

  @Test
  public void testBuildBadRequest() throws JsonProcessingException {
    final ProxyResponse response = ProxyResponseBuilder.buildBadRequest(new Exception());
    final Map<String, Object> parsedBody = OBJECT_MAPPER.readValue(response.body, Map.class);
    assertTrue(parsedBody.containsKey("errors"));
    assertFalse(((List)parsedBody.get("errors")).isEmpty());
    assertTrue(((Map)((List)parsedBody.get("errors")).get(0)).containsKey("code"));
    assertEquals("400", response.statusCode);
  }
}
