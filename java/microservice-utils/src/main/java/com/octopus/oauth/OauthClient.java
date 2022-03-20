package com.octopus.oauth;

/**
 * Represents the access to an OAuth authorization server exposing client credentials.
 */
public interface OauthClient {

  /**
   * Generates a client credential token.
   *
   * @param authorization The authorization, which is basic auth with the client id and secret.
   * @param grantType     The grant type, which is always client_credentials.
   * @param clientId      The client id.
   * @param scope         The Oauth scopes to request.
   * @return The client credentials access token.
   */
  Oauth getToken(
      final String authorization,
      final String grantType,
      final String clientId,
      final String scope);
}
