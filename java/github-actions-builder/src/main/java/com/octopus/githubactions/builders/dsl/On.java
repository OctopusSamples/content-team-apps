package com.octopus.githubactions.builders.dsl;

import lombok.Builder;

/**
 * Represents the top level on property.
 */
@lombok.Data
@Builder
public class On {

  private Push push;
  private WorkflowDispatch workflowDispatch;
}
