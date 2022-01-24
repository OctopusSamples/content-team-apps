package com.octopus.githuboauth.domain.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents the response from the GitHub OAuth service when exchanging a code for a token.
 * https://docs.github.com/en/developers/apps/building-oauth-apps/authorizing-oauth-apps#response
 */
@Data
public class OauthResponse {
  @JsonProperty("access_token")
  private String accessToken;
  private String scope;
  @JsonProperty("token_type")
  private String tokenType;
}
