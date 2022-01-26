package com.octopus.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CaseInsensitiveCookieExtractorTest {
  private static final CaseInsensitiveCookieExtractor CASE_INSENSITIVE_COOKIE_EXTRACTOR = new CaseInsensitiveCookieExtractor();

  @ParameterizedTest
  @CsvSource({"blah,hi", "test,there", "BLAH,hi", "TEST,there"})
  public void TestCookieExtractionMultiValue(final String name, final String value) {
    final List<String> cookie = CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllCookieValues(
        new ImmutableMap.Builder<String, List<String>>().put(
            "Cookie",
            new ImmutableList.Builder<String>()
                .add("blah=hi;test=there")
                .build()
        ).build(),
        new ImmutableMap.Builder<String, String>().build(),
        name);

    assertEquals(value, cookie.get(0));
  }

  @ParameterizedTest
  @CsvSource({"blah,hi", "test,there", "BLAH,hi", "TEST,there"})
  public void TestCookieExtractionMultiValueAwsEvent(final String name, final String value) {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withMultiValueHeaders(new ImmutableMap.Builder<String, List<String>>().put(
            "Cookie",
            new ImmutableList.Builder<String>()
                .add("blah=hi;test=there")
                .build()
        ).build());
    final List<String> values = CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllCookieValues(
        apiGatewayProxyRequestEvent,
        name);

    assertTrue(values.contains(value));
  }

  @ParameterizedTest
  @CsvSource({"blah,hi", "test,there", "BLAH,hi", "TEST,there"})
  public void TestCookieExtractionNullSingleCollection(final String name, final String value) {
    final List<String> cookie = CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllCookieValues(
        new ImmutableMap.Builder<String, List<String>>().put(
            "Cookie",
            new ImmutableList.Builder<String>()
                .add("blah=hi;test=there")
                .build()
        ).build(),
        null,
        name);

    assertEquals(value, cookie.get(0));
  }

  @ParameterizedTest
  @CsvSource({"blah,hi", "test,there", "BLAH,hi", "TEST,there"})
  public void TestCookieExtractionNullMultiCollection(final String name, final String value) {
    final List<String> cookie = CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllCookieValues(
        null,
        new ImmutableMap.Builder<String, String>().put("Cookie", "blah=hi;test=there").build(),
        name);

    assertEquals(value, cookie.get(0));
  }

  @ParameterizedTest
  @CsvSource({"blah,hi", "test,there", "BLAH,hi", "TEST,there"})
  public void TestCookieExtractionSingleValue(final String name, final String value) {
    final List<String> cookie = CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllCookieValues(
        new ImmutableMap.Builder<String, List<String>>().build(),
        new ImmutableMap.Builder<String, String>().put("Cookie", "blah=hi;test=there").build(),
        name);

    assertEquals(value, cookie.get(0));
  }

  @ParameterizedTest
  @CsvSource({"blah,hi", "test,there", "BLAH,hi", "TEST,there"})
  public void TestCookieExtractionSingleValueSpaces(final String name, final String value) {
    final List<String> cookie = CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllCookieValues(
        new ImmutableMap.Builder<String, List<String>>().build(),
        new ImmutableMap.Builder<String, String>().put("Cookie", "blah=hi; test=there").build(),
        name);

    assertEquals(value, cookie.get(0));
  }

  @ParameterizedTest
  @CsvSource({"blah,hi", "test,there", "BLAH,hi", "TEST,there"})
  public void TestCookieExtractionSingleValueAwsEvent(final String name, final String value) {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withHeaders(new ImmutableMap.Builder<String, String>().put("Cookie", "blah=hi; test=there").build());
    final List<String> values = CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllCookieValues(
        apiGatewayProxyRequestEvent,
        name);

    assertTrue(values.contains(value));
  }
}
