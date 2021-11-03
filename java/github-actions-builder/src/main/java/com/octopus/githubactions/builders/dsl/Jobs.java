package com.octopus.githubactions.builders.dsl;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

/**
 * Represents the to level jobs property.
 */
@Data
@Builder
@RegisterForReflection
public class Jobs {

  private Build build;
}
