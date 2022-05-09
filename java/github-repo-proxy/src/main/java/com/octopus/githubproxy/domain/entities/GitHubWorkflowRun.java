package com.octopus.githubproxy.domain.entities;

import com.github.jasminb.jsonapi.LongIdHandler;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents the downstream version of workflow runs: https://docs.github.com/en/rest/actions/workflow-runs.
 */
@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@Type("workflowruns")
public class GitHubWorkflowRun {
  @Id(LongIdHandler.class)
  private Long id;
  private String htmlUrl;
  private String status;
  private Long runNumber;
}
