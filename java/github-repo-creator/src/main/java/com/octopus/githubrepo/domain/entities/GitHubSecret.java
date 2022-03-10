package com.octopus.githubrepo.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A GitHub secret resource.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitHubSecret {
  @JsonProperty("encrypted_value")
  private String encryptedValue;
  @JsonProperty("key_id")
  private String keyId;
}
