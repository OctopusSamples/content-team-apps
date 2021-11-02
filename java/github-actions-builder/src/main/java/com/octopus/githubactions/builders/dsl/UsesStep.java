package com.octopus.githubactions.builders.dsl;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a simple uses step.
 */
@Data
@Builder
public class UsesStep implements Step {
  private String uses;
}
