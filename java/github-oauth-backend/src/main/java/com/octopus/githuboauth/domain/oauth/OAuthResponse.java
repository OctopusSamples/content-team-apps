package com.octopus.githuboauth.domain.oauth;

import lombok.Data;

@Data
public class OAuthResponse {
  private String access_token;
  private int expires_in;
  private String refresh_token;
  private int refresh_token_expires_in;
  private String scope;
  private String token_type;
}
