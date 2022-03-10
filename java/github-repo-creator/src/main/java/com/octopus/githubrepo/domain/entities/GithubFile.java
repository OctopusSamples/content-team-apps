package com.octopus.githubrepo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a github file.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubFile {
  private String message;
  private String content;
  private String branch;
}
