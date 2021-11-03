package com.octopus.githubactions.builders.dsl;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

/**
 * Represents the top level on property.
 */
@Data
@Builder
@RegisterForReflection
public class On {

  private Push push;
  private WorkflowDispatch workflowDispatch;
}
