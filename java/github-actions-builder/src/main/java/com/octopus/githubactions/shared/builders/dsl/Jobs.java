package com.octopus.githubactions.shared.builders.dsl;

import lombok.Builder;
import lombok.Data;

/** Represents the to level jobs property. */
@Data
@Builder
public class Jobs {

  private Build build;
}
