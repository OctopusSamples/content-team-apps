package com.octopus.audits.domain.jsonapi.impl;

import com.octopus.audits.GlobalConstants;
import com.octopus.audits.domain.exceptions.InvalidAcceptHeaders;
import com.octopus.audits.domain.jsonapi.AcceptHeaderVerifier;
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
            .filter(h -> h.startsWith(GlobalConstants.JSONAPI_CONTENT_TYPE))
            .collect(Collectors.toList());

    if (!jsonApiAcceptHeaders.isEmpty() && !jsonApiAcceptHeaders.contains(GlobalConstants.JSONAPI_CONTENT_TYPE)) {
      throw new InvalidAcceptHeaders();
    }
  }
}
