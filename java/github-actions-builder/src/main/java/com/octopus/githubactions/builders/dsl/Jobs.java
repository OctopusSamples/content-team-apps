package com.octopus.githubactions.builders.dsl;

import lombok.Builder;

/**
 * Represents the to level jobs property.
 */
@lombok.Data
@Builder
public class Jobs {

  private Build build;
}
