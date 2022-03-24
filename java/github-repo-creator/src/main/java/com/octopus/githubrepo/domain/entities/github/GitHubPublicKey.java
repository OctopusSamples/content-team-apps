package com.octopus.githubrepo.domain.entities.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * A GitHub public key resource.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
public class GitHubPublicKey {
  @JsonProperty("key_id")
  private String keyId;
  private String key;
}
