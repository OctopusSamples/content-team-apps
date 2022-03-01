package com.octopus.jsonapi.impl;

import com.octopus.Constants;
import com.octopus.exceptions.InvalidAcceptHeaders;
import com.octopus.jsonapi.AcceptHeaderVerifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
            .filter(h -> h.startsWith(Constants.JSONAPI_CONTENT_TYPE)).toList();

    if (!jsonApiAcceptHeaders.isEmpty() && !jsonApiAcceptHeaders.contains(Constants.JSONAPI_CONTENT_TYPE)) {
      throw new InvalidAcceptHeaders();
    }
  }
}
