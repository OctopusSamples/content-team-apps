package com.octopus.githubrepo.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the API key returned by Octopus.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OctopusApiKey {
  @JsonProperty("Id")
  private String id;

  @JsonProperty("Purpose")
  private String purpose;

  @JsonProperty("UserId")
  private String userId;

  @JsonProperty("ApiKey")
  private String apiKey;

  @JsonProperty("Created")
  private String created;

  @JsonProperty("Expires")
  private String expires;
}
