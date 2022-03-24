package com.octopus.githubrepo.domain.utils;

import java.util.Optional;

/**
 * Defines a service for extracting values from the Links header returned by GitHub.
 */
public interface LinksHeaderParsing {

  /**
   * Get the last page.
   *
   * @param link The Links header.
   * @return The last page (if it is found).
   */
  Optional<String> getLastPage(String link);
}
