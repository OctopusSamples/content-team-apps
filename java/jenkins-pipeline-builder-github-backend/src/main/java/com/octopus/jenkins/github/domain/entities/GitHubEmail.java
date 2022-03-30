package com.octopus.jenkins.github.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * The data returned by the GitHub public email endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class GitHubEmail {
  private String email;
}
