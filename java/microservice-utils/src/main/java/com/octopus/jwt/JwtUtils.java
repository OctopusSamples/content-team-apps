package com.octopus.jwt;

import java.util.Optional;

/** A service for extracting JWTs from headers. */
public interface JwtUtils {

  /**
   * Extract the access token from the Authorization header.
   *
   * @param authorizationHeader The Authorization header.
   * @return The access token, or an empty optional if the access token was not found.
   */
  Optional<String> getJwtFromAuthorizationHeader(String authorizationHeader);
}
