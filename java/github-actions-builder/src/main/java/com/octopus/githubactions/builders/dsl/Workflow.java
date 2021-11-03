package com.octopus.githubactions.builders.dsl;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

/**
 * Represents the entire workflow.
 */
@Data
@Builder
@RegisterForReflection
public class Workflow {

  private String name;
  private On on;
  private Jobs jobs;
}
