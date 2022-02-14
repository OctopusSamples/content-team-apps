package com.octopus.audits.domain.utilities.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.octopus.audits.GlobalConstants;
import com.octopus.audits.domain.utilities.JwtVerifier;
import io.quarkus.logging.Log;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * An implementation of JwtVerifier using Jose JWT.
 */
@ApplicationScoped
public class JoseJwtVerifier implements JwtVerifier {

  private static final String COGNITO_GROUPS = "cognito:groups";
  private static final String SCOPE = "scope";
  private static final String CLIENT_ID = "client_id";

  @ConfigProperty(name = "cognito.jwk-base64")
  Optional<String> cognitoJwk;

  @ConfigProperty(name = "cognito.disable-auth")
  boolean cognitoDisableAuth;

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean jwtContainsScope(final String jwt, final String claim, final String clientId) {
    if (!configIsValid()) {
      return false;
    }

    try {
      if (jwtIsValid(jwt, cognitoJwk.get())) {
        if (extractClaims(jwt).contains(claim)) {
          final boolean valid = extractClientId(jwt).map(c -> c.equals(clientId)).orElse(false);
          if (!valid) {
            Log.error(GlobalConstants.MICROSERVICE_NAME
                + "-Jwt-AuthorizationError Service-Authorization token does not match the expected Cognito client");
          }
          return valid;
        }
      }
    } catch (final IOException | ParseException | JOSEException e) {
      Log.error(GlobalConstants.MICROSERVICE_NAME + "-Jwt-ValidationError", e);
    }

    return false;
  }

  /**
   * Extracts the claims from the JWT.
   *
   * @param jwt The JWT.
   * @return The list of scopes.
   * @throws ParseException If the string couldn't be parsed to a JWS object.
   */
  public List<String> extractClaims(final String jwt) throws ParseException {
    final Map<String, Object> payload = getPayload(jwt);
    if (payload.containsKey(SCOPE)) {
      return Arrays.asList(payload.get(SCOPE).toString().split(" "));
    }
    return List.of();
  }

  /**
   * Extracts the Cognito client id from the token.
   *
   * @param jwt The access token.
   * @return The client ID if it was found.
   * @throws ParseException If the string couldn't be parsed to a JWS object.
   */
  public Optional<String> extractClientId(final String jwt) throws ParseException {
    final Map<String, Object> payload = getPayload(jwt);
    if (payload.containsKey(CLIENT_ID)) {
      return Optional.of(payload.get(CLIENT_ID).toString());
    }
    return Optional.empty();
  }

  final Map<String, Object> getPayload(final String jwt) throws ParseException {
    final JWSObject jwsObject = JWSObject.parse(jwt);
    return jwsObject.getPayload().toJSONObject();
  }

  private boolean configIsValid() {
    return cognitoDisableAuth
        || (cognitoJwk.isPresent()
        && StringUtils.isNotEmpty(cognitoJwk.get()));
  }

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
