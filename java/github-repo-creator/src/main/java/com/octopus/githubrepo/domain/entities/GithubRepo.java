package com.octopus.githubrepo.domain.entities;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a new repository to be created in GitHub.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubRepo {
  @NotBlank
  private String name;
}
