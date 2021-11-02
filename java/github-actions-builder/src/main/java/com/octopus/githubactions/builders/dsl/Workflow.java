package com.octopus.githubactions.builders.dsl;

import lombok.Builder;

/**
 * Represents the entire workflow.
 */
@lombok.Data
@Builder
public class Workflow {

  private String name;
  private On on;
  private Jobs jobs;
}
