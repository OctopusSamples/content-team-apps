package com.octopus.lambda;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CaseInsensitiveLambdaHttpValueExtractorTest {

  private static final CaseInsensitiveLambdaHttpValueExtractor CASE_INSENSITIVE_LAMBDA_HTTP_VALUE_EXTRACTOR
      = new CaseInsensitiveLambdaHttpValueExtractor();

  @ParameterizedTest
  @CsvSource({"query,value", "query,value2", "QUERY,value", "QUERY,value2"})
  public void TestCookieExtractionMultiValue(final String name, final String value) {
    final List<String> values = CASE_INSENSITIVE_LAMBDA_HTTP_VALUE_EXTRACTOR.getAllQueryParams(
        new ImmutableMap.Builder<String, List<String>>().put(
            "query",
            new ImmutableList.Builder<String>()
                .add("value")
                .add("value2")
                .build()
        ).build(),
        new ImmutableMap.Builder<String, String>().build(),
        name);

    assertTrue(values.contains(value));
  }

  @ParameterizedTest
  @CsvSource({"query,value", "query,value2", "QUERY,value", "QUERY,value2"})
  public void TestCookieExtractionMultiValueAwsEvent(final String name, final String value) {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withMultiValueQueryStringParameters(new ImmutableMap.Builder<String, List<String>>().put(
            "query",
            new ImmutableList.Builder<String>()
                .add("value")
                .add("value2")
                .build()
        ).build());
    final List<String> values = CASE_INSENSITIVE_LAMBDA_HTTP_VALUE_EXTRACTOR.getAllQueryParams(
        apiGatewayProxyRequestEvent,
        name);

    assertTrue(values.contains(value));
  }

  @ParameterizedTest
  @CsvSource({"query,value", "QUERY,value"})
  public void TestCookieExtractionSingleValue(final String name, final String value) {
    final List<String> values = CASE_INSENSITIVE_LAMBDA_HTTP_VALUE_EXTRACTOR.getAllQueryParams(
        new ImmutableMap.Builder<String, List<String>>().build(),
        new ImmutableMap.Builder<String, String>().put("query", "value").build(),
        name);

    assertTrue(values.contains(value));
  }

  @ParameterizedTest
  @CsvSource({"query,value", "QUERY,value"})
  public void TestCookieExtractionSingleValueAwsEvent(final String name, final String value) {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withQueryStringParameters(new ImmutableMap.Builder<String, String>().put("query", "value").build());
    final List<String> values = CASE_INSENSITIVE_LAMBDA_HTTP_VALUE_EXTRACTOR.getAllQueryParams(
        apiGatewayProxyRequestEvent,
        name);

    assertTrue(values.contains(value));
  }
}
