package com.octopus.audits.domain.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.audits.domain.utilities.JwtUtils;
import org.junit.jupiter.api.Test;

public class JwtUtilsTest {
  private static final JwtUtils JWT_UTILS = new JwtUtils();

  @Test
  public void tokenExtractionTest() {
    assertEquals("abcdefg", JWT_UTILS.getJwtFromAuthorizationHeader("Bearer abcdefg").get());
    assertEquals("abcdefg", JWT_UTILS.getJwtFromAuthorizationHeader("Bearer abcdefg, Bearer hijklmnop").get());
    assertEquals("abcdefg", JWT_UTILS.getJwtFromAuthorizationHeader("bearer abcdefg").get());
    assertEquals("abcdefg", JWT_UTILS.getJwtFromAuthorizationHeader("BEARER abcdefg").get());
    assertEquals("abcdefg", JWT_UTILS.getJwtFromAuthorizationHeader("bEaReR abcdefg").get());
    assertEquals("abcdefg", JWT_UTILS.getJwtFromAuthorizationHeader("bEaReR  abcdefg").get());
    assertEquals("abcdefg", JWT_UTILS.getJwtFromAuthorizationHeader("bEaReR  abcdefg ").get());
    assertEquals("abcdefg", JWT_UTILS.getJwtFromAuthorizationHeader(" bEaReR abcdefg").get());
    assertTrue( JWT_UTILS.getJwtFromAuthorizationHeader("basic abcdefg").isEmpty());
  }
}
