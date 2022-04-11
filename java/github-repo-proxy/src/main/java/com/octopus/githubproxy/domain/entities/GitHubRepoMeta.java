package com.octopus.githubproxy.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metadata associated with a GitHub repo.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GitHubRepoMeta {
  private String browsableUrl;
}
