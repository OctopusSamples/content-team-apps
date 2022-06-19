package com.octopus.products.domain.auth;

/**
 * Defaines a service used to determine if a request is authorized.
 */
public interface Authorizer {
  /**
   * Determines if the supplied token grants the required scopes to execute the operation.
   *
   * @param authorizationHeader        The Authorization header.
   * @param serviceAuthorizationHeader The Service-Authorization header.
   * @return true if the request is authorized, and false otherwise.
   */
  boolean isAuthorized(final String authorizationHeader, final String serviceAuthorizationHeader);
}
