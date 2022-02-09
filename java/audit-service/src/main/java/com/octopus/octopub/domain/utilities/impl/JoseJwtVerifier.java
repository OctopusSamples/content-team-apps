package com.octopus.octopub.domain.utilities.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.octopus.octopub.GlobalConstants;
import com.octopus.octopub.domain.utilities.JwtVerifier;
import io.quarkus.logging.Log;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/** An implementation of JwtVerifier using Jose JWT. */
@ApplicationScoped
public class JoseJwtVerifier implements JwtVerifier {

  private static final String COGNITO_GROUPS = "cognito:groups";
  private static final String SCOPE = "scope";

  @ConfigProperty(name = "cognito.pool")
  Optional<String> cognitoPool;

  @ConfigProperty(name = "cognito.region")
  Optional<String> cognitoRegion;

  @ConfigProperty(name = "cognito.disable-auth")
  boolean cognitoDisableAuth;

  /** {@inheritDoc} */
  @Override
  public boolean jwtContainsCognitoGroup(final String jwt, final String group) {
    if (!configIsValid()) {
      return false;
    }

    try {
      if (jwtIsValid(jwt)) {
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
      if (jwtIsValid(jwt)) {
        final JWSObject jwsObject = JWSObject.parse(jwt);
        final Map<String, Object> payload = jwsObject.getPayload().toJSONObject();
        if (payload.containsKey(SCOPE)) {
          return ArrayUtils.contains(payload.get(SCOPE).toString().split(" "), claim);
        }
      }
    } catch (final IOException | ParseException | JOSEException e) {
      Log.error(GlobalConstants.MICROSERVICE_NAME + "-Jwt-ValidationError", e);
    }

    return false;
  }

  private boolean configIsValid() {
    return cognitoDisableAuth
        || cognitoRegion.isEmpty()
        || cognitoPool.isEmpty()
        || StringUtils.isEmpty(cognitoPool.get())
        || StringUtils.isEmpty(cognitoRegion.get());
  }

  private boolean jwtIsValid(final String jwt)
      throws ParseException, IOException, JOSEException {
    final JWSObject jwsObject = JWSObject.parse(jwt);
    final JWKSet publicKeys =
        JWKSet.load(
            new URL(
                "https://cognito-idp."
                    + cognitoRegion.get().trim()
                    + ".amazonaws.com/"
                    + cognitoPool.get().trim()
                    + "/.well-known/jwks.json"));
    final JWSVerifier verifier = new RSASSAVerifier(publicKeys.getKeyByKeyId(jwsObject.getHeader().getKeyID()).toRSAKey());
    return jwsObject.verify(verifier);
  }
}
