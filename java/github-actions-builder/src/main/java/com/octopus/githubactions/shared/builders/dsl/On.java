package com.octopus.githubactions.shared.builders.dsl;

import lombok.Builder;
import lombok.Data;

/** Represents the top level on property. */
@Data
@Builder
public class On {

  private Push push;
  private WorkflowDispatch workflowDispatch;
}
