package com.octopus.githubproxy.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * The GitHub repo.
 *
 * <p>https://docs.github.com/en/rest/reference/repos#get-a-repository.
 */
@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class Repo {
  private String name;
  private RepoOwner owner;
}
