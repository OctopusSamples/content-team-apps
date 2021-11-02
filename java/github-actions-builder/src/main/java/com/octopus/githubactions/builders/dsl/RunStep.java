package com.octopus.githubactions.builders.dsl;

import lombok.Builder;

/**
 * Represents a shell run step.
 */
@lombok.Data
@Builder
public class RunStep implements Step {

  private String name;
  private String id;
  private String run;
  private String shell;
}
