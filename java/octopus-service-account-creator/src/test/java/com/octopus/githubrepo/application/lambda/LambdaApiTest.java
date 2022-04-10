package com.octopus.githubrepo.application.lambda;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.octopus.Constants;
import com.octopus.lambda.RequestMatcher;
import com.octopus.lambda.impl.RequestMatcherImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LambdaApiTest {
  private static final String API_ENDPOINT = "/api/serviceaccounts";
  private static final String HEALTH_ENDPOINT = "/health/serviceaccounts";
  private static final RequestMatcher REQUEST_MATCHER = new RequestMatcherImpl();

  @ParameterizedTest
  @ValueSource(strings = {HEALTH_ENDPOINT + "/POST"})
  public void testHealthRequestMatching(final String path) {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("GeT");
    event.setPath(path);
    assertTrue(REQUEST_MATCHER.requestIsMatch(event, ServiceAccountApi.HEALTH_RE, Constants.Http.GET_METHOD));
  }

  @Test
  public void testRootRequestMatching() {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("GeT");
    event.setPath(API_ENDPOINT);
    assertTrue(REQUEST_MATCHER.requestIsMatch(event, ServiceAccountApi.ROOT_RE, Constants.Http.GET_METHOD));
  }

  @Test
  public void testCreateRequestMatching() {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("PoSt");
    event.setPath(API_ENDPOINT);
    assertTrue(REQUEST_MATCHER.requestIsMatch(event, ServiceAccountApi.ROOT_RE, Constants.Http.POST_METHOD));
  }

  @Test
  public void testNullParams() {
    assertThrows(NullPointerException.class, () -> {
      REQUEST_MATCHER.requestIsMatch(new APIGatewayProxyRequestEvent(), ServiceAccountApi.ROOT_RE, null);
    });

    assertThrows(NullPointerException.class, () -> {
      REQUEST_MATCHER.requestIsMatch(new APIGatewayProxyRequestEvent(), null, "");
    });

    assertThrows(NullPointerException.class, () -> {
      REQUEST_MATCHER.requestIsMatch(null, ServiceAccountApi.ROOT_RE, "");
    });
  }
}
