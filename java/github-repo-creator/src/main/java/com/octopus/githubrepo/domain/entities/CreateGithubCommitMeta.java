package com.octopus.githubrepo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Metadata associated with a GitHub commit.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class CreateGithubCommitMeta {

  /**
   * Represents the browsable address of the repo that contains the new commit.
   */
  private String browsableRepoUrl;
  /**
   * Represents the API address of the repo that contains the new commit.
   */
  private String apiRepoUrl;
  /**
   * The repo owner.
   */
  private String owner;
  /**
   * The repo name.
   */
  private String repoName;
}
