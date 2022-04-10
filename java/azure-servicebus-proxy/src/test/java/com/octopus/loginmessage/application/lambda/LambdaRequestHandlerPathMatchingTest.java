package com.octopus.loginmessage.application.lambda;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.octopus.Constants;
import com.octopus.lambda.RequestMatcher;
import com.octopus.lambda.impl.RequestMatcherImpl;
import com.octopus.loginmessage.application.TestPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LambdaRequestHandlerPathMatchingTest {

  private static final RequestMatcher REQUEST_MATCHER = new RequestMatcherImpl();

  @ParameterizedTest
  @ValueSource(strings = {
      TestPaths.HEALTH_ENDPOINT + "/POST"})
  public void testHealthRequestMatching(final String path) {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("GeT");
    event.setPath(path);
    assertTrue(REQUEST_MATCHER.requestIsMatch(event, LambdaRequestHanlder.HEALTH_RE, Constants.Http.GET_METHOD));
  }

  @Test
  public void testCreateRequestMatching() {
    final APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("PoSt");
    event.setPath(TestPaths.API_ENDPOINT);
    assertTrue(REQUEST_MATCHER.requestIsMatch(event, LambdaRequestHanlder.ROOT_RE, Constants.Http.POST_METHOD));
  }

  @Test
  public void testNullParams() {
    assertThrows(NullPointerException.class, () -> {
      REQUEST_MATCHER.requestIsMatch(new APIGatewayProxyRequestEvent(), LambdaRequestHanlder.ROOT_RE, null);
    });

    assertThrows(NullPointerException.class, () -> {
      REQUEST_MATCHER.requestIsMatch(new APIGatewayProxyRequestEvent(), null, "");
    });

    assertThrows(NullPointerException.class, () -> {
      REQUEST_MATCHER.requestIsMatch(null, LambdaRequestHanlder.ROOT_RE, "");
    });
  }
}
