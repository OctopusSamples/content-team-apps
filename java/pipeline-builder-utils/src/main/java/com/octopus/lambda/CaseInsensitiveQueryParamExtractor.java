package com.octopus.lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;

public class CaseInsensitiveQueryParamExtractor implements QueryParamExtractor {

  /**
   * Gets headers from every collection they might be in.
   *
   * @param multiQuery The map containing paarms with multiple values.
   * @param query The map containing query params with one value.
   * @param header The name of the header.
   * @return The list of header values.
   */
  public List<String> getAllQueryParams(
      final Map<String, List<String>> multiQuery,
      final Map<String, String> query,
      @NonNull final String header) {
    final List<String> values = new ArrayList<String>(getMultiQuery(multiQuery, header));
    values.addAll(getQuery(query, header));
    return values;
  }

  /**
   * Headers are case insensitive, but the maps we get from Lambda are case sensitive, so we need to
   * have some additional logic to get the available headers.
   *
   * @param query The list of query params
   * @param header The name of the query param to return
   * @return The list of query params
   */
  public List<String> getMultiQuery(
      final Map<String, List<String>> query, @NonNull final String header) {
    if (query == null) {
      return List.of();
    }

    return query.entrySet().stream()
        .filter(e -> header.equalsIgnoreCase(e.getKey()))
        .flatMap(e -> e.getValue().stream())
        .collect(Collectors.toList());
  }

  /**
   * Headers are case insensitive, but the maps we get from Lambda are case sensitive, so we need to
   * have some additional logic to get the available headers.
   *
   * @param query The list of query params
   * @param header The name of the header to return
   * @return The list of header values
   */
  public List<String> getQuery(final Map<String, String> query, @NonNull final String header) {
    if (query == null) {
      return List.of();
    }

    return query.entrySet().stream()
        .filter(e -> header.equalsIgnoreCase(e.getKey()))
        .map(e -> e.getValue())
        .collect(Collectors.toList());
  }
}
