package com.octopus.lambda.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;

/**
 * An implementation of LambdaHttpCookieExtractor that extracts cookies from headers.
 */
public class CaseInsensitiveCookieExtractor implements LambdaHttpCookieExtractor {

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getCookieValue(@NonNull final APIGatewayProxyRequestEvent input,
      @NonNull final String name) {
    return getAllCookieValues(input.getMultiValueHeaders(), input.getHeaders(), name)
        .stream().findFirst();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getAllCookieValues(@NonNull final APIGatewayProxyRequestEvent input,
      @NonNull final String name) {
    return getAllCookieValues(input.getMultiValueHeaders(), input.getHeaders(), name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getAllCookieValues(
      final Map<String, List<String>> multiQuery,
      final Map<String, String> query,
      @NonNull final String name) {
    final List<String> values = new ArrayList<String>(getMultiCookie(multiQuery, name));
    values.addAll(getCookie(query, name));
    return values;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getMultiCookie(
      final Map<String, List<String>> query, @NonNull final String name) {
    if (query == null) {
      return List.of();
    }

    return extractCookieValue(name,
        query.entrySet().stream()
            .filter(e -> "Cookie".equalsIgnoreCase(e.getKey()))
            .flatMap(e -> e.getValue().stream()))
        .collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getCookie(final Map<String, String> query, @NonNull final String name) {
    if (query == null) {
      return List.of();
    }

    return extractCookieValue(name,
        query.entrySet().stream()
            .filter(e -> "Cookie".equalsIgnoreCase(e.getKey()))
            .map(Entry::getValue))
        .collect(Collectors.toList());
  }

  private Stream<String> extractCookieValue(final String name, final Stream<String> stream) {
    return stream
        .flatMap(h -> Stream.of(h.split(";")))
        .map(String::trim)
        .filter(c -> c.split("=")[0].equalsIgnoreCase(name))
        .map(c -> c.replaceFirst(c.split("=")[0] + "=", ""));
  }
}
