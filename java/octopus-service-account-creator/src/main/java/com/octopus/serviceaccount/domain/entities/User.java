package com.octopus.serviceaccount.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a user returned from the Octopus API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
  @JsonProperty("Id")
  private String id;
  @JsonProperty("Username")
  private String username;
  @JsonProperty("DisplayName")
  private String displayName;
}
