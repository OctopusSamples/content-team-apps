package com.octopus.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CaseInsensitiveCookieExtractorTest {
  private static final CaseInsensitiveCookieExtractor CASE_INSENSITIVE_COOKIE_EXTRACTOR = new CaseInsensitiveCookieExtractor();

  @Test
  public void TestCookieExtractionMultiValue() {
    final List<String> cookie = CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllQueryParams(
        new ImmutableMap.Builder<String, List<String>>().put(
            "Cookie",
            new ImmutableList.Builder<String>()
                .add("blah=hi;test=there")
                .build()
        ).build(),
        new ImmutableMap.Builder<String, String>().build(),
        "blah");

    assertEquals("hi", cookie.get(0));
  }

  @Test
  public void TestCookieExtractionSingleValue() {
    final List<String> cookie = CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllQueryParams(
        new ImmutableMap.Builder<String, List<String>>().build(),
        new ImmutableMap.Builder<String, String>().put("Cookie", "blah=hi;test=there").build(),
        "blah");

    assertEquals("hi", cookie.get(0));
  }

  @Test
  public void TestCookieExtractionSingleValueSpaces() {
    final List<String> cookie = CASE_INSENSITIVE_COOKIE_EXTRACTOR.getAllQueryParams(
        new ImmutableMap.Builder<String, List<String>>().build(),
        new ImmutableMap.Builder<String, String>().put("Cookie", "blah=hi; test=there").build(),
        "blah");

    assertEquals("hi", cookie.get(0));
  }
}
