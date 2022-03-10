package com.octopus.githubrepo.domain.utils;

/**
 * A service that determines if a request is authorized.
 */
public interface ServiceAuthUtils {
  /**
   * Determines if the supplied token grants the required scopes to execute the operation.
   *
   * @param authorizationHeader        The Authorization header.
   * @param serviceAuthorizationHeader The Service-Authorization header.
   * @return true if the request is authorized, and false otherwise.
   */
  boolean isAuthorized(
      final String authorizationHeader,
      final String serviceAuthorizationHeader);
}
