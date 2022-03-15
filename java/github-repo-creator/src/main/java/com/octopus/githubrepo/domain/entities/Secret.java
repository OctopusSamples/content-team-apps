package com.octopus.githubrepo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a secret to be included in a new GitHub repo.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Secret {
  private String name;
  private String value;
  private boolean encrypted;
}
