package com.octopus.githubrepo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents a secret to be included in a new GitHub repo.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Jacksonized
public class Secret {
  private String name;
  private String value;
  private boolean encrypted;
}
