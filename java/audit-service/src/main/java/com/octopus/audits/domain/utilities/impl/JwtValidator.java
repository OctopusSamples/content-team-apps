package com.octopus.audits.domain.utilities.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import org.apache.commons.io.IOUtils;

@ApplicationScoped
public class JwtValidator {
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
  public boolean jwtIsValid(final String jwt, final String jwk)
      throws ParseException, IOException, JOSEException {
    final JWSObject jwsObject = JWSObject.parse(jwt);
    final String jwkDecoded = new String(Base64.getDecoder().decode(jwk));
    final JWKSet publicKeys = JWKSet.load(
        IOUtils.toInputStream(jwkDecoded, Charset.defaultCharset()));
    final JWK key = publicKeys.getKeyByKeyId(jwsObject.getHeader().getKeyID());
    if (key != null) {
      final JWSVerifier verifier = new RSASSAVerifier(key.toRSAKey());
      if (jwsObject.verify(verifier)) {
        final Map<String, Object> payload = jwsObject.getPayload().toJSONObject();
        if (payload.containsKey("exp")) {
          return ((Long) payload.get("exp") * 1000) > new Date().getTime();
        }
      }
    }

    return false;
  }
}
