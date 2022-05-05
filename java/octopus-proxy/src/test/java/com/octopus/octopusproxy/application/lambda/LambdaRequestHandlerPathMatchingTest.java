package com.octopus.octopusproxy.application.lambda;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.octopus.Constants;
import com.octopus.octopusproxy.application.Paths;
import com.octopus.octopusproxy.application.lambda.impl.LambdaRequestHandlerGetOne;
import com.octopus.octopusproxy.application.lambda.impl.LambdaRequestHandlerHealth;
import com.octopus.lambda.RequestMatcher;
import com.octopus.lambda.impl.RequestMatcherImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * These tests verify the path matching behaviour of the Lambda request handler.
 */
public class LambdaRequestHandlerPathMatchingTest {

  private static final RequestMatcher REQUEST_MATCHER = new RequestMatcherImpl();

  @ParameterizedTest
  @ValueSource(strings = {
      Paths.HEALTH_ENDPOINT + "/x/GET"})
  public void testHealthRequestMatching(final String path) {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("GeT");
    event.setPath(path);
    assertTrue(REQUEST_MATCHER.requestIsMatch(event, LambdaRequestHandlerHealth.HEALTH_RE, Constants.Http.GET_METHOD));
  }

  @Test
  public void testIndividualRequestMatching() {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("GeT");
    event.setPath(Paths.API_ENDPOINT + "/1");
    assertTrue(REQUEST_MATCHER.requestIsMatch(event, LambdaRequestHandlerGetOne.INDIVIDUAL_RE, Constants.Http.GET_METHOD));
  }

  @Test
  public void testNullParams() {
    assertThrows(NullPointerException.class, () -> {
      REQUEST_MATCHER.requestIsMatch(new APIGatewayProxyRequestEvent(),
          LambdaRequestHandlerGetOne.INDIVIDUAL_RE, null);
    });

    assertThrows(NullPointerException.class, () -> {
      REQUEST_MATCHER.requestIsMatch(new APIGatewayProxyRequestEvent(), null, "");
    });

    assertThrows(NullPointerException.class, () -> {
      REQUEST_MATCHER.requestIsMatch(null, LambdaRequestHandlerGetOne.INDIVIDUAL_RE, "");
    });
  }
}
