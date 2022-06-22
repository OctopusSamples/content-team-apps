package com.octopus.audits.domain.features;

import java.util.List;

/**
 * A feature that defines the untrusted actions that can be audited with no auth.
 */
public interface UntrustedActions {

  /**
   * Gets the list of untrusted actions.
   *
   * @return the list of untrusted actions.
   */
  public List<String> getUntrustedActions();
}
