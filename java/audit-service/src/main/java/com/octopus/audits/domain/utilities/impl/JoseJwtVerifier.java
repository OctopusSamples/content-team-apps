package com.octopus.audits.domain.utilities.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.octopus.audits.GlobalConstants;
import com.octopus.audits.domain.utilities.JwtVerifier;
import io.quarkus.logging.Log;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/** An implementation of JwtVerifier using Jose JWT. */
@ApplicationScoped
public class JoseJwtVerifier implements JwtVerifier {

  private static final String COGNITO_GROUPS = "cognito:groups";
  private static final String SCOPE = "scope";

  @ConfigProperty(name = "cognito.jwk")
  Optional<String> cognitoJwk;

  @ConfigProperty(name = "cognito.disable-auth")
  boolean cognitoDisableAuth;

  /** {@inheritDoc} */
  @Override
  public boolean jwtContainsCognitoGroup(final String jwt, final String group) {
    if (!configIsValid()) {
      return false;
    }

    try {
      if (jwtIsValid(jwt, cognitoJwk.get())) {
        final JWSObject jwsObject = JWSObject.parse(jwt);
        final Map<String, Object> payload = jwsObject.getPayload().toJSONObject();
        if (payload.containsKey(COGNITO_GROUPS)) {
          if (payload.get(COGNITO_GROUPS) instanceof List) {
            return ((List) payload.get(COGNITO_GROUPS))
                .stream().anyMatch(g -> g.toString().equals(group));
          }
        }
      }
    } catch (final IOException | ParseException | JOSEException e) {
      Log.error(GlobalConstants.MICROSERVICE_NAME + "-Jwt-ValidationError", e);
    }

    return false;
  }

  @Override
  public boolean jwtContainsClaim(final String jwt, final String claim) {
    if (!configIsValid()) {
      return false;
    }

    try {
      if (jwtIsValid(jwt, cognitoJwk.get())) {
          return extractClaims(jwt).contains(claim);
      }
    } catch (final IOException | ParseException | JOSEException e) {
      Log.error(GlobalConstants.MICROSERVICE_NAME + "-Jwt-ValidationError", e);
    }

    return false;
  }

  public List<String> extractClaims(final String jwt) throws ParseException {
      final JWSObject jwsObject = JWSObject.parse(jwt);
      final Map<String, Object> payload = jwsObject.getPayload().toJSONObject();
      if (payload.containsKey(SCOPE)) {
        return Arrays.asList(payload.get(SCOPE).toString().split(" "));
      }
      return List.of();
  }

  private boolean configIsValid() {
    return cognitoDisableAuth
        || cognitoJwk.isEmpty()
        || StringUtils.isEmpty(cognitoJwk.get());
  }

  public boolean jwtIsValid(final String jwt, final String jwk)
      throws ParseException, IOException, JOSEException {
    final JWSObject jwsObject = JWSObject.parse(jwt);
    final JWKSet publicKeys = JWKSet.load(IOUtils.toInputStream(jwk, Charset.defaultCharset()));
    final JWSVerifier verifier = new RSASSAVerifier(publicKeys.getKeyByKeyId(jwsObject.getHeader().getKeyID()).toRSAKey());
    if (jwsObject.verify(verifier)) {
      final Map<String, Object> payload = jwsObject.getPayload().toJSONObject();
      if (payload.containsKey("exp")) {
        return (Long)payload.get("exp") > new Date().getTime();
      }
    }

    return false;
  }
}
