package com.octopus.jwt;

import java.util.Optional;

/**
 * An interface exposing methods used to verify a request contains the correct authorization.
 */
public interface JwtInspector {

  /**
   * Confirms if the JWT contains the specified Cognito group.
   *
   * @param jwt   The JWT passed with the request.
   * @param group The group to find in the JWT.
   * @return true if the group is found, and false otherwise.
   */
  boolean jwtContainsCognitoGroup(String jwt, String group);

  /**
   * Confirms if the JWT contains the specified claim.
   *
   * @param jwt      The JWT passed with the request.
   * @param claim    The claim to find in the JWT.
   * @param clientId The client id that must be used to create the token.
   * @return true if the claim is found, and false otherwise.
   */
  boolean jwtContainsScope(String jwt, String claim, final String clientId);

  /**
   * Returns the claim if it exists.
   *
   * @param jwt   The JWT.
   * @param claim The name of the claim.
   * @return The claim value, or empty if it doesn't exist.
   */
  Optional<String> getClaim(String jwt, String claim);
}
