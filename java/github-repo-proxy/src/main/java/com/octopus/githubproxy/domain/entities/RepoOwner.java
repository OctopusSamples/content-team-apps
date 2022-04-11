package com.octopus.githubproxy.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * The owner property attached to a repo.
 *
 * <p>https://docs.github.com/en/rest/reference/repos#get-a-repository.
 */
@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class RepoOwner {
  private String login;
}
