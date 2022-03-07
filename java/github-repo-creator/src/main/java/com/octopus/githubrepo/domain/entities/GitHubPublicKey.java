package com.octopus.githubrepo.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitHubPublicKey {
  @JsonProperty("key_id")
  private String keyId;
  private String key;
}
