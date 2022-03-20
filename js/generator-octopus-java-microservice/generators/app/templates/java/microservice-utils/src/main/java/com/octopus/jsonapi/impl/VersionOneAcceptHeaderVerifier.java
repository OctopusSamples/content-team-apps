package com.octopus.jsonapi.impl;

import com.octopus.Constants;
import com.octopus.exceptions.InvalidAcceptHeadersException;
import com.octopus.jsonapi.AcceptHeaderVerifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Checks the "accept" headers match version 1 of the JSONAPI spec.
 */
public class VersionOneAcceptHeaderVerifier implements AcceptHeaderVerifier {

  /**
   * {@inheritDoc}
   */
  public void checkAcceptHeader(final List<String> acceptHeader) {
    if (acceptHeader == null || acceptHeader.isEmpty()) {
      return;
    }

    final List<String> jsonApiAcceptHeaders =
        acceptHeader.stream()
            .filter(Objects::nonNull)
            .flatMap(h -> Arrays.stream(h.split(",")))
            .map(String::trim)
            .filter(h -> h.startsWith(Constants.JsonApi.JSONAPI_CONTENT_TYPE))
            .collect(Collectors.toList());

    if (!jsonApiAcceptHeaders.isEmpty() && !jsonApiAcceptHeaders.contains(Constants.JsonApi.JSONAPI_CONTENT_TYPE)) {
      throw new InvalidAcceptHeadersException();
    }
  }
}
