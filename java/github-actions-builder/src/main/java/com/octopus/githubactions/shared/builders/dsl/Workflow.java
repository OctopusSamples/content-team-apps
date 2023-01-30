package com.octopus.githubactions.shared.builders.dsl;

import lombok.Builder;
import lombok.Data;

/** Represents the entire workflow. */
@Data
@Builder
public class Workflow {

  private Permissions permissions;
  private String name;
  private On on;
  private Jobs jobs;
}
