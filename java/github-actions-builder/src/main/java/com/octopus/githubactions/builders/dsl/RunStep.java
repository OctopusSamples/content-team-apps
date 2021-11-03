package com.octopus.githubactions.builders.dsl;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a shell run step.
 */
@Data
@Builder
@RegisterForReflection
public class RunStep implements Step {

  private String name;
  private String id;
  private String run;
  private String shell;
}
