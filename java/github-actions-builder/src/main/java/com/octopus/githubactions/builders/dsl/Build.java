package com.octopus.githubactions.builders.dsl;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Represents the build property under jobs.
 */
@Data
@Builder
@RegisterForReflection
public class Build {

  private String runsOn;
  private List<Step> steps;
}
