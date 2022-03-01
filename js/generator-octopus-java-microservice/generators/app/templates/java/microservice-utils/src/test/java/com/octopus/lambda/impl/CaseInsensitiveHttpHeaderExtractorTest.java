package com.octopus.lambda.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class CaseInsensitiveHttpHeaderExtractorTest {
  private static final LambdaHttpHeaderExtractor CASE_INSENSITIVE_HTTP_HEADER_EXTRACTOR = new CaseInsensitiveHttpHeaderExtractor();

  @Test
  public void getHeaderWithAwsEvent() {
    final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent()
        .withHeaders(new ImmutableMap.Builder<String, String>()
            .put("key", "value")
            .build());
    assertEquals("value", CASE_INSENSITIVE_HTTP_HEADER_EXTRACTOR.getFirstHeader(input, "key").get());
  }

  @Test
  public void getFirstHeaderWithNull() {
    final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent()
        .withHeaders(new ImmutableMap.Builder<String, String>()
            .put("key", "value")
            .build());
    assertThrows(NullPointerException.class, () -> CASE_INSENSITIVE_HTTP_HEADER_EXTRACTOR.getFirstHeader(null, "key"));
    assertThrows(NullPointerException.class, () -> CASE_INSENSITIVE_HTTP_HEADER_EXTRACTOR.getFirstHeader(input, null));
  }

  @Test
  public void getHeaderWithMap() {
    final Map<String, String> value = new ImmutableMap.Builder<String, String>()
        .put("key", "value")
        .build();
    assertTrue(CASE_INSENSITIVE_HTTP_HEADER_EXTRACTOR.getHeader(value, "key").contains("value"));
  }

  @Test
  public void getHeaderWithNull() {
    final Map<String, String> value = new ImmutableMap.Builder<String, String>()
        .put("key", "value")
        .build();
    assertThrows(NullPointerException.class, () -> CASE_INSENSITIVE_HTTP_HEADER_EXTRACTOR.getHeader(value, null));
  }

  @Test
  public void getAllCookieValuesWithAwsEvent() {
    final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent()
        .withMultiValueHeaders(new ImmutableMap.Builder<String, List<String>>()
            .put("key", new ImmutableList.Builder<String>()
                .add("value1")
                .add("value2")
                .build())
            .build());
    assertTrue(CASE_INSENSITIVE_HTTP_HEADER_EXTRACTOR.getAllHeaders(input, "key").contains("value1"));
    assertTrue(CASE_INSENSITIVE_HTTP_HEADER_EXTRACTOR.getAllHeaders(input, "key").contains("value2"));
  }

  @Test
  public void getAllCookieValueWithNull() {
    final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent()
        .withMultiValueHeaders(new ImmutableMap.Builder<String, List<String>>()
            .put("Cookie", new ImmutableList.Builder<String>()
                .add("value1")
                .add("value2")
                .build())
            .build());
    assertThrows(NullPointerException.class, () -> CASE_INSENSITIVE_HTTP_HEADER_EXTRACTOR.getAllHeaders(null, "key"));
    assertThrows(NullPointerException.class, () -> CASE_INSENSITIVE_HTTP_HEADER_EXTRACTOR.getAllHeaders(input, null));
  }

  @Test
  public void getAllCookieValueWithNull2() {
    final Map<String, List<String>> multiValues = new ImmutableMap.Builder<String, List<String>>()
        .put("key", new ImmutableList.Builder<String>()
            .add("value1")
            .add("value2")
            .build())
        .build();
    final Map<String, String> singleValues = new ImmutableMap.Builder<String, String>()
        .put("key", "value")
        .build();
    assertThrows(NullPointerException.class, () -> CASE_INSENSITIVE_HTTP_HEADER_EXTRACTOR.getAllHeaders(multiValues, singleValues, null));
  }

  @Test
  public void getMultiCookieWithMap() {
    final Map<String, List<String>> values = new ImmutableMap.Builder<String, List<String>>()
        .put("key", new ImmutableList.Builder<String>()
            .add("value1")
            .add("value2")
            .build())
        .build();
    assertTrue(CASE_INSENSITIVE_HTTP_HEADER_EXTRACTOR.getMultiHeader(values, "key").contains("value1"));
    assertTrue(CASE_INSENSITIVE_HTTP_HEADER_EXTRACTOR.getMultiHeader(values, "key").contains("value2"));
  }

  @Test
  public void getMultiCookieWithNull() {
    final Map<String, List<String>> values = new ImmutableMap.Builder<String, List<String>>()
        .put("key", new ImmutableList.Builder<String>()
            .add("value1")
            .add("value2")
            .build())
        .build();
    assertThrows(NullPointerException.class, () -> CASE_INSENSITIVE_HTTP_HEADER_EXTRACTOR.getMultiHeader(values, null));
  }
}
