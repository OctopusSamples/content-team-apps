package com.octopus.jwt;

import com.nimbusds.jose.JOSEException;
import java.io.IOException;
import java.text.ParseException;

/**
 * A service used to validate JWTs.
 */
public interface JwtValidator {

  /**
   * Verify the JWT token has the correct signature and is not expired.
   *
   * @param jwt The JWT.
   * @param jwk The JWK, base64 encoded.
   * @return true if the JWT is valid, false otherwise.
   * @throws ParseException If the string couldn't be parsed to a JWS object.
   * @throws IOException    If the input stream couldn't be read.
   * @throws JOSEException  If the RSA JWK extraction failed.
   */
  boolean jwtIsValid(String jwt, String jwk)
      throws ParseException, IOException, JOSEException;
}
