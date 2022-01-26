package com.octopus.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
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
  public Optional<String> getQueryParam(APIGatewayProxyRequestEvent input, String name) {
    return getAllQueryParams(input.getMultiValueHeaders(), input.getHeaders(), name)
        .stream().findFirst();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getAllQueryParams(@NonNull final APIGatewayProxyRequestEvent input,
      @NonNull final String name) {
    return getAllQueryParams(input.getMultiValueHeaders(), input.getHeaders(), name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getAllQueryParams(
      final Map<String, List<String>> multiQuery,
      final Map<String, String> query,
      @NonNull final String name) {
    final List<String> values = new ArrayList<String>(getMultiQuery(multiQuery, name));
    values.addAll(getQuery(query, name));
    return values;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getMultiQuery(
      final Map<String, List<String>> query, @NonNull final String name) {
    if (query == null) {
      return List.of();
    }

    return getCookieValue(name,
        query.entrySet().stream()
            .filter(e -> "Cookie".equalsIgnoreCase(e.getKey()))
            .flatMap(e -> e.getValue().stream()))
        .collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getQuery(final Map<String, String> query, @NonNull final String name) {
    if (query == null) {
      return List.of();
    }

    return getCookieValue(name,
        query.entrySet().stream()
            .filter(e -> "Cookie".equalsIgnoreCase(e.getKey()))
            .map(Entry::getValue))
        .collect(Collectors.toList());
  }

  private Stream<String> getCookieValue(final String name, final Stream<String> stream) {
    return stream
        .flatMap(h -> Stream.of(h.split(";")))
        .map(String::trim)
        .filter(c -> c.split("=")[0].equalsIgnoreCase(name))
        .map(c -> c.replaceFirst(c.split("=")[0] + "=", ""));
  }
}
