package com.octopus.githubactions.github.domain.entities;

import lombok.Builder;
import lombok.Data;

/**
 * The data returned by the GitHub public email endpoint.
 */
@Data
@Builder
public class GitHubEmail {
  private String email;
}
