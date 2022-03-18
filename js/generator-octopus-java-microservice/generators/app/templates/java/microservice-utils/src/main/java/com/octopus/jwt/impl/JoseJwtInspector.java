package com.octopus.jwt.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.octopus.features.CognitoJwkBase64Feature;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtValidator;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 * An implementation of JwtVerifier using Jose JWT.
 */
public class JoseJwtInspector implements JwtInspector {

  private static Logger LOGGER = Logger.getLogger(JoseJwtInspector.class.getName());
  private static final String COGNITO_GROUPS = "cognito:groups";
  private static final String SCOPE = "scope";
  private static final String CLIENT_ID = "client_id";
  private final CognitoJwkBase64Feature cognitoJwk;
  private final DisableSecurityFeature cognitoDisableAuth;
  private final JwtValidator jwtValidator;
  private final MicroserviceNameFeature microserviceName;

  /**
   * Constructor.
   *
   * @param cognitoJwk         The cognito JWK feature.
   * @param cognitoDisableAuth The cognito disable auth feature.
   * @param jwtValidator       The JWT validator.
   * @param microserviceName   The microservice name feature.
   */
  public JoseJwtInspector(
      @NonNull final CognitoJwkBase64Feature cognitoJwk,
      @NonNull final DisableSecurityFeature cognitoDisableAuth,
      @NonNull final JwtValidator jwtValidator,
      @NonNull final MicroserviceNameFeature microserviceName) {
    this.cognitoJwk = cognitoJwk;
    this.cognitoDisableAuth = cognitoDisableAuth;
    this.jwtValidator = jwtValidator;
    this.microserviceName = microserviceName;
  }

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
              LOGGER.log(Level.SEVERE, microserviceName.getMicroserviceName()
                  + "-Jwt-AuthorizationError Authorization token does not contain the group");
            }
            return valid;
          }
        }
      }
    } catch (final IOException | ParseException | JOSEException e) {
      LOGGER.log(Level.SEVERE,
          microserviceName.getMicroserviceName() + "-Jwt-ValidationError " + jwt, e);
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
            LOGGER.log(Level.SEVERE, microserviceName.getMicroserviceName()
                + "-Jwt-ServiceAuthorizationError Service-Authorization token does not match the expected Cognito client");
          }
          return valid;
        } else {
          LOGGER.log(Level.SEVERE, microserviceName.getMicroserviceName()
              + "-Jwt-ServiceAuthorizationError Service-Authorization token does not contain the required scope "
              + scope);
        }
      }
    } catch (final IOException | ParseException | JOSEException e) {
      LOGGER.log(Level.SEVERE,
          microserviceName.getMicroserviceName() + "-Jwt-ServiceValidationError", e);
    }

    return false;
  }

  @Override
  public Optional<String> getClaim(@NonNull final String jwt, @NonNull final String claim) {
    try {
      final Map<String, Object> payload = getPayload(jwt);
      if (payload.containsKey(claim)) {
        return Optional.of(payload.get(claim).toString());
      }
    } catch (ParseException e) {
      LOGGER.log(Level.SEVERE,
          microserviceName.getMicroserviceName() + "-Jwt-ClaimExtraction", e);
    }

    return Optional.empty();
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
        && StringUtils.isNotBlank(cognitoJwk.getCognitoJwk().get()));
  }
}
