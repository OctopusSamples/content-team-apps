package com.octopus.octopusoauth.domain.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response from the Octopus OAuth login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OauthResponse {

  @JsonProperty("state")
  private String state;
  @JsonProperty("id_token")
  private String idToken;
}
