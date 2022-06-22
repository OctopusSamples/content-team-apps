package com.octopus.audits.domain.features.impl;

import com.octopus.audits.domain.features.UntrustedActions;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * The implementation of the UntrustedActions feature.
 */
@ApplicationScoped
public class UntrustedActionsImpl implements UntrustedActions {

  @ConfigProperty(name = "audit.untrsuted-actions")
  Optional<String> untrustedActions;

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getUntrustedActions() {
    return untrustedActions
        .stream()
        .flatMap(a -> Arrays.stream(a.split(",")))
        .map(String::trim)
        .filter(StringUtils::isBlank)
        .collect(Collectors.toList());
  }
}
