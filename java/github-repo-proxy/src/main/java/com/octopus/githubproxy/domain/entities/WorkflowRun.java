package com.octopus.githubproxy.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class WorkflowRun {
  private Long id;
  @JsonProperty("html_url")
  private String htmlUrl;
  private String status;
  @JsonProperty("run_number")
  private Long runNumber;
}
