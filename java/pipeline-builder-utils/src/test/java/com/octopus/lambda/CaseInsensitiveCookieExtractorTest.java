package com.octopus.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    final List<String> cookie = CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllQueryParams(
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
  public void TestCookieExtractionSingleValue(final String name, final String value) {
    final List<String> cookie = CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllQueryParams(
        new ImmutableMap.Builder<String, List<String>>().build(),
        new ImmutableMap.Builder<String, String>().put("Cookie", "blah=hi;test=there").build(),
        name);

    assertEquals(value, cookie.get(0));
  }

  @ParameterizedTest
  @CsvSource({"blah,hi", "test,there", "BLAH,hi", "TEST,there"})
  public void TestCookieExtractionSingleValueSpaces(final String name, final String value) {
    final List<String> cookie = CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllQueryParams(
        new ImmutableMap.Builder<String, List<String>>().build(),
        new ImmutableMap.Builder<String, String>().put("Cookie", "blah=hi; test=there").build(),
        name);

    assertEquals(value, cookie.get(0));
  }
}
