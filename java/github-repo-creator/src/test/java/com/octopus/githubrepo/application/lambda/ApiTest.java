package com.octopus.githubrepo.application.lambda;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.octopus.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ApiTest {
  private static final String API_ENDPOINT = "/api/populategithubrepo";
  private static final String HEALTH_ENDPOINT = "/health/populategithubrepo";
  private static final PopulateGithubRepoApi API = new PopulateGithubRepoApi();

  @ParameterizedTest
  @ValueSource(strings = {HEALTH_ENDPOINT + "/POST"})
  public void testHealthRequestMatching(final String path) {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("GeT");
    event.setPath(path);
    assertTrue(API.requestIsMatch(event, API.HEALTH_RE, Constants.Http.GET_METHOD));
  }

  @ParameterizedTest
  @ValueSource(strings = {HEALTH_ENDPOINT + "/POST"})
  public void testInvalidMethodHealthRequestMatching(final String path) {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("POST");
    event.setPath(path);
    assertFalse(API.requestIsMatch(event, API.HEALTH_RE, Constants.Http.GET_METHOD));
  }

  @ParameterizedTest
  @ValueSource(strings = {HEALTH_ENDPOINT + "/Invalid"})
  public void testInvalidPathHealthRequestMatching(final String path) {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("GET");
    event.setPath(path);
    assertFalse(API.requestIsMatch(event, API.HEALTH_RE, Constants.Http.GET_METHOD));
  }

  @Test
  public void testRootRequestMatching() {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("GeT");
    event.setPath(API_ENDPOINT);
    assertTrue(API.requestIsMatch(event, API.ROOT_RE, Constants.Http.GET_METHOD));
  }

  @Test
  public void testCreateRequestMatching() {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("PoSt");
    event.setPath(API_ENDPOINT);
    assertTrue(API.requestIsMatch(event, API.ROOT_RE, Constants.Http.POST_METHOD));
  }

  @Test
  public void testNullParams() {
    assertThrows(NullPointerException.class, () -> {
      API.requestIsMatch(new APIGatewayProxyRequestEvent(), API.ROOT_RE, null);
    });

    assertThrows(NullPointerException.class, () -> {
      API.requestIsMatch(new APIGatewayProxyRequestEvent(), null, "");
    });

    assertThrows(NullPointerException.class, () -> {
      API.requestIsMatch(null, API.ROOT_RE, "");
    });
  }
}
