package com.octopus.audits.application.lambda;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.octopus.audits.GlobalConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AuditApiTest {
  private static final AuditApi AUDIT_API = new AuditApi();

  @ParameterizedTest
  @ValueSource(strings = {"/health/audits/GET", "/health/audits/POST", "/health/audits/x/GET"})
  public void testHealthRequestMatching(final String path) {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("GeT");
    event.setPath(path);
    assertTrue(AUDIT_API.requestIsMatch(event, AuditApi.HEALTH_RE, GlobalConstants.GET_METHOD));
  }

  @Test
  public void testRootRequestMatching() {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("GeT");
    event.setPath("/api/audits");
    assertTrue(AUDIT_API.requestIsMatch(event, AuditApi.ROOT_RE, GlobalConstants.GET_METHOD));
  }

  @Test
  public void testIndividualRequestMatching() {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("GeT");
    event.setPath("/api/audits/1");
    assertTrue(AUDIT_API.requestIsMatch(event, AuditApi.INDIVIDUAL_RE, GlobalConstants.GET_METHOD));
  }

  @Test
  public void testCreateRequestMatching() {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("PoSt");
    event.setPath("/api/audits");
    assertTrue(AUDIT_API.requestIsMatch(event, AuditApi.ROOT_RE, GlobalConstants.POST_METHOD));
  }

  @Test
  public void testNullParams() {
    assertThrows(NullPointerException.class, () -> {
      AUDIT_API.requestIsMatch(new APIGatewayProxyRequestEvent(), AuditApi.ROOT_RE, null);
    });

    assertThrows(NullPointerException.class, () -> {
      AUDIT_API.requestIsMatch(new APIGatewayProxyRequestEvent(), null, "");
    });

    assertThrows(NullPointerException.class, () -> {
      AUDIT_API.requestIsMatch(null, AuditApi.ROOT_RE, "");
    });
  }
}
