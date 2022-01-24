package com.octopus.githubactions.builders.dsl;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** Represents the build property under jobs. */
@Data
@Builder
public class Build {

  private String runsOn;
  private List<Step> steps;
}
