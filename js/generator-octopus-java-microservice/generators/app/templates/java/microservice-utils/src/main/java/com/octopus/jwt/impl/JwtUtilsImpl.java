package com.octopus.jwt.impl;

import com.octopus.jwt.JwtUtils;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/** A utility class for extracting JWTs from headers. */
public class JwtUtilsImpl implements JwtUtils {
  private static final String BEARER = "bearer";

  @Override
  public Optional<String> getJwtFromAuthorizationHeader(final String authorizationHeader) {
    if (StringUtils.isBlank(authorizationHeader)) {
      return Optional.empty();
    }

    if (!authorizationHeader.toLowerCase().trim().startsWith(BEARER + " ")) {
      return Optional.empty();
    }

    // Assume the first header is the one we want if for some reason we got a comma separated list
    final String token = authorizationHeader.split(",")[0]
        .trim()
        .replaceFirst("(?i)" + BEARER + " ", "")
        .trim();

    if (StringUtils.isNotBlank(token)) {
      return Optional.of(token);
    }

    return Optional.empty();
  }
}
