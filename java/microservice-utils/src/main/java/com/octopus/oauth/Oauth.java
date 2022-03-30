package com.octopus.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the value returned by Cognito when generating an OAuth token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Oauth {
  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("expires_in")
  private int expiresIn;
  @JsonProperty("token_type")
  private String tokenType;
}
