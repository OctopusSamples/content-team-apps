package com.octopus.githubproxy.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Metadata associated with a GitHub repo.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class GitHubRepoMeta {
  private String browsableUrl;
}
