package com.octopus.githuboauth.domain.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents the response from the GitHub OAuth service when exchanging a code for a token.
 * https://docs.github.com/en/developers/apps/building-oauth-apps/authorizing-oauth-apps#response
 */
public class OauthResponse {

  private String accessToken;
  private String scope;
  private String tokenType;

  @JsonProperty("access_token")
  public String getAccessToken() {
    return accessToken;
  }

  @JsonProperty("access_token")
  public void setAccessToken(final String accessToken) {
    this.accessToken = accessToken;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(final String scope) {
    this.scope = scope;
  }

  @JsonProperty("token_type")
  public String getTokenType() {
    return tokenType;
  }

  @JsonProperty("token_type")
  public void setTokenType(final String tokenType) {
    this.tokenType = tokenType;
  }
}
