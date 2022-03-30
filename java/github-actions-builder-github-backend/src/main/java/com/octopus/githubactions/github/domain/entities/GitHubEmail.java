package com.octopus.githubactions.github.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The data returned by the GitHub public email endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubEmail {
  private String email;
}
