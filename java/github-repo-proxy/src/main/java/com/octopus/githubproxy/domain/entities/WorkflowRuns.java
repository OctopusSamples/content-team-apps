package com.octopus.githubproxy.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents workflow runs: https://docs.github.com/en/rest/actions/workflow-runs.
 */
@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRuns {
  @JsonProperty("workflow_runs")
  private List<WorkflowRun> workflowRuns;
}
