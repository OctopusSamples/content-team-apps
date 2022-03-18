package com.octopus.customers.application.lambda;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.octopus.Constants;
import com.octopus.customers.application.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LambdaRequestHandlerPathMatchingTest {

  private static final LambdaRequestHanlder API = new LambdaRequestHanlder();

  @ParameterizedTest
  @ValueSource(strings = {
      Paths.HEALTH_ENDPOINT + "/GET", Paths.HEALTH_ENDPOINT + "/POST", Paths.HEALTH_ENDPOINT + "/x/GET"})
  public void testHealthRequestMatching(final String path) {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("GeT");
    event.setPath(path);
    assertTrue(API.requestIsMatch(event, API.HEALTH_RE, Constants.Http.GET_METHOD));
  }

  @Test
  public void testRootRequestMatching() {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("GeT");
    event.setPath(Paths.API_ENDPOINT);
    assertTrue(API.requestIsMatch(event, API.ROOT_RE, Constants.Http.GET_METHOD));
  }

  @Test
  public void testIndividualRequestMatching() {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("GeT");
    event.setPath(Paths.API_ENDPOINT + "/1");
    assertTrue(API.requestIsMatch(event, API.INDIVIDUAL_RE, Constants.Http.GET_METHOD));
  }

  @Test
  public void testCreateRequestMatching() {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("PoSt");
    event.setPath(Paths.API_ENDPOINT);
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
