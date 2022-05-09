package com.octopus.githubproxy.domain.entities;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Meta;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

/**
 * A JSONAPI resource representing the GitHub Repo. This is a much simplified version of what the
 * upstream API generates.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Type("githubrepos")
public class GitHubRepo {

  /**
   * The ID of an external resource is the URL to the GET endpoint that represents the resource.
   */
  @Id
  @URL
  private String id;

  /**
   * The customers first name.
   */
  @NotBlank
  private String owner;

  /**
   * The customers last name.
   */
  @NotBlank
  private String repo;

  /**
   * The workflow runs associated with this repo.
   */
  @Relationship(value = "workflowRuns")
  private List<GitHubWorkflowRun> workflowRuns;

  /**
   * The metadata associated with the repo.
   */
  @Meta
  private GitHubRepoMeta meta;
}
