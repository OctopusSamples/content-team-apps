package com.octopus.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * An implementation of LambdaHttpValueExtractor that ignores value name case.
 */
public class CaseInsensitiveHttpHeaderExtractor implements LambdaHttpHeaderExtractor {

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getHeaderParam(@NonNull final APIGatewayProxyRequestEvent input,
      @NonNull final String name) {
    return getAllHeaderParams(input.getMultiValueHeaders(),
        input.getHeaders(), name)
        .stream().findFirst();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getAllHeaderParams(@NonNull final APIGatewayProxyRequestEvent input,
      @NonNull final String name) {
    return getAllHeaderParams(
        input.getMultiValueHeaders(),
        input.getHeaders(),
        name);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getAllHeaderParams(
      final Map<String, List<String>> multiHeader,
      final Map<String, String> header,
      @NonNull final String name) {
    final List<String> values = new ArrayList<String>(getMultiHeader(multiHeader, name));
    values.addAll(getHeader(header, name));
    return values;
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getMultiHeader(
      final Map<String, List<String>> header, @NonNull final String name) {
    if (header == null) {
      return List.of();
    }

    return header.entrySet().stream()
        .filter(e -> name.equalsIgnoreCase(e.getKey()))
        .flatMap(e -> e.getValue().stream())
        .collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getHeader(final Map<String, String> header, @NonNull final String name) {
    if (header == null) {
      return List.of();
    }

    return header.entrySet().stream()
        .filter(e -> name.equalsIgnoreCase(e.getKey()))
        .map(e -> e.getValue())
        .collect(Collectors.toList());
  }
}
