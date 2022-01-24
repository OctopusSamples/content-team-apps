package com.octopus.lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * An implementation of QueryParamExtractor that ignores query param case.
 */
public class CaseInsensitiveQueryParamExtractor implements QueryParamExtractor {

  /** {@inheritDoc} */
  public List<String> getAllQueryParams(
      final Map<String, List<String>> multiQuery,
      final Map<String, String> query,
      @NonNull final String name) {
    final List<String> values = new ArrayList<String>(getMultiQuery(multiQuery, name));
    values.addAll(getQuery(query, name));
    return values;
  }

  /** {@inheritDoc} */
  public List<String> getMultiQuery(
      final Map<String, List<String>> query, @NonNull final String name) {
    if (query == null) {
      return List.of();
    }

    return query.entrySet().stream()
        .filter(e -> name.equalsIgnoreCase(e.getKey()))
        .flatMap(e -> e.getValue().stream())
        .collect(Collectors.toList());
  }

  /** {@inheritDoc} */
  public List<String> getQuery(final Map<String, String> query, @NonNull final String name) {
    if (query == null) {
      return List.of();
    }

    return query.entrySet().stream()
        .filter(e -> name.equalsIgnoreCase(e.getKey()))
        .map(e -> e.getValue())
        .collect(Collectors.toList());
  }
}
