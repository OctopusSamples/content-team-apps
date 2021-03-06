package com.octopus.githubactions.shared.builders.dsl;

import lombok.Builder;
import lombok.Data;

/** Represents a shell run step. */
@Data
@Builder
public class RunStep implements Step {

  private String name;
  private String id;
  private String run;
  private String shell;
  private String workingDirectory;
}
