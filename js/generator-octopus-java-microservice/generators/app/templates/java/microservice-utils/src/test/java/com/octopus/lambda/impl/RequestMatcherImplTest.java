package com.octopus.lambda.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.octopus.lambda.RequestMatcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;


public class RequestMatcherImplTest {

  private static final RequestMatcher REQUEST_MATCHER = new RequestMatcherImpl();

  @Test
  public void testRequestMatching() {
    assertTrue(REQUEST_MATCHER.requestIsMatch(
        new APIGatewayProxyRequestEvent()
            .withPath("/api/test")
            .withHttpMethod("GET"),
        Pattern.compile("/api/test"),
        "GET"));

    assertTrue(REQUEST_MATCHER.requestIsMatch(
        new APIGatewayProxyRequestEvent()
            .withPath("/api/test")
            .withHttpMethod("GET"),
        Pattern.compile("/api/test/?"),
        "GET"));

    assertTrue(REQUEST_MATCHER.requestIsMatch(
        new APIGatewayProxyRequestEvent()
            .withPath("/api/test/")
            .withHttpMethod("GET"),
        Pattern.compile("/api/test/?"),
        "GET"));

    assertTrue(REQUEST_MATCHER.requestIsMatch(
        new APIGatewayProxyRequestEvent()
            .withPath("/api/test")
            .withHttpMethod("GET"),
        Pattern.compile("/api/test"),
        "GeT"));

    assertTrue(REQUEST_MATCHER.requestIsMatch(
        new APIGatewayProxyRequestEvent()
            .withPath("/api/test")
            .withHttpMethod("get"),
        Pattern.compile("/api/test"),
        "GeT"));

    assertFalse(REQUEST_MATCHER.requestIsMatch(
        new APIGatewayProxyRequestEvent()
            .withPath("/api/test")
            .withHttpMethod("GET"),
        Pattern.compile("/api/test"),
        "POST"));

    assertFalse(REQUEST_MATCHER.requestIsMatch(
        new APIGatewayProxyRequestEvent()
            .withPath("/api/test")
            .withHttpMethod("GET"),
        Pattern.compile("/api/blah"),
        "GET"));
  }

  @Test
  public void testNullParams() {
    assertThrows(NullPointerException.class, () -> {
      REQUEST_MATCHER.requestIsMatch(new APIGatewayProxyRequestEvent(), Pattern.compile("/api/blah"), null);
    });

    assertThrows(NullPointerException.class, () -> {
      REQUEST_MATCHER.requestIsMatch(new APIGatewayProxyRequestEvent(), null, "");
    });

    assertThrows(NullPointerException.class, () -> {
      REQUEST_MATCHER.requestIsMatch(null, Pattern.compile("/api/blah"), "");
    });
  }
}
