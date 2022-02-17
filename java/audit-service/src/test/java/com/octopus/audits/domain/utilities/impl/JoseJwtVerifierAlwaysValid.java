package com.octopus.audits.domain.utilities.impl;

import java.util.Optional;

/**
 * A specialization of the JoseJwtVerifier class that assumes all JWTs are valid. This allows us
 * to test expired JWTs.
 */
public class JoseJwtVerifierAlwaysValid extends JoseJwtVerifier {

  JoseJwtVerifierAlwaysValid() {
    cognitoJwk = Optional.of("");
  }

  public boolean jwtIsValid(final String jwt, final String jwk) {
    return true;
  }

  protected boolean configIsValid() {
    return true;
  }
}
