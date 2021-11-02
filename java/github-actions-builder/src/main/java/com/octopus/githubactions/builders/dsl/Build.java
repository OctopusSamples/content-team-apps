package com.octopus.githubactions.builders.dsl;

import java.util.List;
import lombok.Builder;

/**
 * Represents the build property under jobs.
 */
@lombok.Data
@Builder
public class Build {

  private String runsOn;
  private List<Step> steps;
}
