package com.octopus.jwt.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class JoseJwtConfigTest {

  private static final JoseJwtInspector JWT_INSPECTOR = new JoseJwtInspector(
      () -> Optional.empty(),
      () -> true,
      (jwt, jwk) -> false,
      () -> "test"
  );

  @Test
  public void verifyClaimsExtraction() {
    assertTrue(JWT_INSPECTOR.configIsValid());
  }
}
