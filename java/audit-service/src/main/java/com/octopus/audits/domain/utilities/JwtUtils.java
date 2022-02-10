package com.octopus.audits.domain.utilities;

import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

/** A utility class for extracting JWTs from headers. */
@ApplicationScoped
public class JwtUtils {
  private static final String BEARER = "Bearer";

  /**
   * Extract the JWT from the Authorization header.
   *
   * @param authorizationHeader The Authorization header.
   * @return The JWT, or an empty optional if the JWT was not found.
   */
  public Optional<String> getJwtFromAuthorizationHeader(final String authorizationHeader) {
    if (StringUtils.isBlank(authorizationHeader)) {
      return Optional.empty();
    }

    if (!authorizationHeader.trim().startsWith(BEARER + ":")) {
      return Optional.empty();
    }

    return Optional.of(authorizationHeader.trim().replaceFirst(BEARER + ":", ""));
  }
}
