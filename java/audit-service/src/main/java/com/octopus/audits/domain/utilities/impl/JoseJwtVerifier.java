package com.octopus.audits.domain.utilities.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.octopus.audits.GlobalConstants;
import com.octopus.audits.domain.features.CognitoJwkBase64Feature;
import com.octopus.audits.domain.features.DisableSecurityFeature;
import com.octopus.audits.domain.utilities.JwtVerifier;
import io.quarkus.logging.Log;
import java.io.Console;
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
import javax.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * An implementation of JwtVerifier using Jose JWT.
 */
@ApplicationScoped
public class JoseJwtVerifier implements JwtVerifier {

  private static final String COGNITO_GROUPS = "cognito:groups";
  private static final String SCOPE = "scope";
  private static final String CLIENT_ID = "client_id";

  @Inject
  CognitoJwkBase64Feature cognitoJwk;

  @Inject
  DisableSecurityFeature cognitoDisableAuth;

  @Inject
  JwtValidator jwtValidator;

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean jwtContainsCognitoGroup(final String jwt, final String group) {
    if (!configIsValid()) {
      return false;
    }

    try {
      if (jwtValidator.jwtIsValid(jwt, cognitoJwk.getCognitoJwk().get())) {
        final JWSObject jwsObject = JWSObject.parse(jwt);
        final Map<String, Object> payload = jwsObject.getPayload().toJSONObject();
        if (payload.containsKey(COGNITO_GROUPS)) {
          if (payload.get(COGNITO_GROUPS) instanceof List) {
            final boolean valid = ((List) payload.get(COGNITO_GROUPS))
                .stream().anyMatch(g -> g.toString().equals(group));
            if (!valid) {
              Log.error(GlobalConstants.MICROSERVICE_NAME
                  + "-Jwt-AuthorizationError Authorization token does not contain the group");
            }
            return valid;
          }
        }
      }
    } catch (final IOException | ParseException | JOSEException e) {
      Log.error(GlobalConstants.MICROSERVICE_NAME + "-Jwt-ValidationError " + jwt, e);
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean jwtContainsScope(final String jwt, final String scope, final String clientId) {
    if (!configIsValid()) {
      return false;
    }

    try {
      if (jwtValidator.jwtIsValid(jwt, cognitoJwk.getCognitoJwk().get())) {
        if (extractScope(jwt).contains(scope)) {
          final boolean valid = extractClientId(jwt).map(c -> c.equals(clientId)).orElse(false);
          if (!valid) {
            Log.error(GlobalConstants.MICROSERVICE_NAME
                + "-Jwt-ServiceAuthorizationError Service-Authorization token does not match the expected Cognito client");
          }
          return valid;
        } else {
          Log.error(GlobalConstants.MICROSERVICE_NAME
              + "-Jwt-ServiceAuthorizationError Service-Authorization token does not contain the required scope "
              + scope);
        }
      }
    } catch (final IOException | ParseException | JOSEException e) {
      Log.error(GlobalConstants.MICROSERVICE_NAME + "-Jwt-ServiceValidationError", e);
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
  List<String> extractScope(final String jwt) throws ParseException {
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
  Optional<String> extractClientId(final String jwt) throws ParseException {
    final Map<String, Object> payload = getPayload(jwt);
    if (payload.containsKey(CLIENT_ID)) {
      return Optional.of(payload.get(CLIENT_ID).toString());
    }
    return Optional.empty();
  }

  Map<String, Object> getPayload(final String jwt) throws ParseException {
    final JWSObject jwsObject = JWSObject.parse(jwt);
    return jwsObject.getPayload().toJSONObject();
  }

  protected boolean configIsValid() {
    return cognitoDisableAuth.getCognitoAuthDisabled()
        || (cognitoJwk.getCognitoJwk().isPresent()
        && StringUtils.isNotEmpty(cognitoJwk.getCognitoJwk().get()));
  }


}
