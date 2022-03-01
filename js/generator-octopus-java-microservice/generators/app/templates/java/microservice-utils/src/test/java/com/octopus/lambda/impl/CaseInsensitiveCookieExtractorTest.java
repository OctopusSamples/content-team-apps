package com.octopus.lambda.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class CaseInsensitiveCookieExtractorTest {
  private static final LambdaHttpCookieExtractor CASE_INSENSITIVE_COOKIE_EXTRACTOR = new CaseInsensitiveCookieExtractor();

  @Test
  public void getCookieValueWithAwsEvent() {
    final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent()
        .withHeaders(new ImmutableMap.Builder<String, String>()
            .put("Cookie", "key=value")
            .build());
    assertEquals("value", CASE_INSENSITIVE_COOKIE_EXTRACTOR.getCookieValue(input, "key").get());
  }

  @Test
  public void getCookieValueWithNull() {
    final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent()
        .withHeaders(new ImmutableMap.Builder<String, String>()
            .put("Cookie", "key=value")
            .build());
    assertThrows(NullPointerException.class, () -> CASE_INSENSITIVE_COOKIE_EXTRACTOR.getCookieValue(null, "key"));
    assertThrows(NullPointerException.class, () -> CASE_INSENSITIVE_COOKIE_EXTRACTOR.getCookieValue(input, null));
  }

  @Test
  public void getCookieWithMap() {
    final Map<String, String> value = new ImmutableMap.Builder<String, String>()
        .put("Cookie", "key=value")
        .build();
    assertTrue(CASE_INSENSITIVE_COOKIE_EXTRACTOR.getCookie(value, "key").contains("value"));
  }

  @Test
  public void getCookieWithNull() {
    final Map<String, String> value = new ImmutableMap.Builder<String, String>()
        .put("Cookie", "key=value")
        .build();
    assertThrows(NullPointerException.class, () -> CASE_INSENSITIVE_COOKIE_EXTRACTOR.getCookie(value, null));
  }

  @Test
  public void getAllCookieValuesWithAwsEvent() {
    final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent()
        .withMultiValueHeaders(new ImmutableMap.Builder<String, List<String>>()
            .put("Cookie", new ImmutableList.Builder<String>()
                .add("key=value1")
                .add("key=value2")
                .build())
            .build());
    assertTrue(CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllCookieValues(input, "key").contains("value1"));
    assertTrue(CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllCookieValues(input, "key").contains("value2"));
  }

  @Test
  public void getAllCookieValueWithNull() {
    final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent()
        .withMultiValueHeaders(new ImmutableMap.Builder<String, List<String>>()
            .put("Cookie", new ImmutableList.Builder<String>()
                .add("key=value1")
                .add("key=value2")
                .build())
            .build());
    assertThrows(NullPointerException.class, () -> CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllCookieValues(null, "key"));
    assertThrows(NullPointerException.class, () -> CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllCookieValues(input, null));
  }

  @Test
  public void getAllCookieValueWithNull2() {
    final Map<String, List<String>> multiValues = new ImmutableMap.Builder<String, List<String>>()
        .put("Cookie", new ImmutableList.Builder<String>()
            .add("key=value1")
            .add("key=value2")
            .build())
        .build();
    final Map<String, String> singleValues = new ImmutableMap.Builder<String, String>()
        .put("Cookie", "key=value")
        .build();
    assertThrows(NullPointerException.class, () -> CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllCookieValues(multiValues, singleValues, null));
  }

  @Test
  public void getMultiCookieWithMap() {
    final Map<String, List<String>> values = new ImmutableMap.Builder<String, List<String>>()
        .put("Cookie", new ImmutableList.Builder<String>()
            .add("key=value1")
            .add("key=value2")
            .build())
        .build();
    assertTrue(CASE_INSENSITIVE_COOKIE_EXTRACTOR.getMultiCookie(values, "key").contains("value1"));
    assertTrue(CASE_INSENSITIVE_COOKIE_EXTRACTOR.getMultiCookie(values, "key").contains("value2"));
  }

  @Test
  public void getMultiCookieWithNull() {
    final Map<String, List<String>> values = new ImmutableMap.Builder<String, List<String>>()
        .put("Cookie", new ImmutableList.Builder<String>()
            .add("key=value1")
            .add("key=value2")
            .build())
        .build();
    assertThrows(NullPointerException.class, () -> CASE_INSENSITIVE_COOKIE_EXTRACTOR.getMultiCookie(values, null));
  }
}
