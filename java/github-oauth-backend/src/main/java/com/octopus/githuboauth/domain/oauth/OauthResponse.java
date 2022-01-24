package com.octopus.githuboauth.domain.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents the response from the GitHub OAuth service when exchanging a code for a token.
 */
@Data
public class OauthResponse {
  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("expires_in")
  private int expiresIn;
  @JsonProperty("refresh_token")
  private String refreshToken;
  @JsonProperty("refresh_token_expires_in")
  private int refreshTokenExpiresIn;
  private String scope;
  @JsonProperty("token_type")
  private String tokenType;
}
