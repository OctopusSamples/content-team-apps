package com.octopus.githubrepo.domain.utils;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;

/**
 * A service used to log into Octopus with an ID token.
 */
public interface OctopusLoginUtils {

  /**
   * Get the CSRF value from the associated cookie.
   */
  Optional<String> getCsrf(List<String> cookieHeaders);

  /**
   * Extract any useful cookies from the response. Note this is not a robust cookie parser. For
   * example, expiry dates are ignored. We just know the few cookies that Octopus considers
   * important, and ignore the rest.
   */
  List<String> getCookies(Response response);

  /**
   * The state hash uses the "OctoState" salt.
   *
   * @param state The state string.
   * @return The slated, hashed, and base64 encoded version of the state.
   */
  String getStateHash(String state);

  /**
   * The nonce hash uses the "OctoNonce" salt.
   *
   * @param idToken The id token.
   * @return The slated, hashed, and base64 encoded version of the nonce found from the id token.
   */
  String getNonceHash(String idToken);

  /**
   * This is a workaround to use a REST client with a variable base URL.
   */
  Response logIn(URI apiUri, String idToken, String state,
      String stateHash, String nonceHash);
}
