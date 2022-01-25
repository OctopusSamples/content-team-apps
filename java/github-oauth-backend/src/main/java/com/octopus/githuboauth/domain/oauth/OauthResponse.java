package com.octopus.githuboauth.domain.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents the response from the GitHub OAuth service when exchanging a code for a token.
 * https://docs.github.com/en/developers/apps/building-oauth-apps/authorizing-oauth-apps#response
 */
public class OauthResponse {

  @JsonProperty("access_token")
  private String accessToken;
  private String scope;
  @JsonProperty("token_type")
  private String tokenType;
  private String error;

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(final String accessToken) {
    this.accessToken = accessToken;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(final String scope) {
    this.scope = scope;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(final String tokenType) {
    this.tokenType = tokenType;
  }

  public String getError() {
    return error;
  }

  public void setError(final String error) {
    this.error = error;
  }
}
