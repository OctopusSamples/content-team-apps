package com.octopus.githubactions.github.domain.exceptions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Verifies the exception constructors.
 */
public class ExceptionsTest {
  @Test
  public void testArgumentNulls() {
    assertThrows(NullPointerException.class, () -> new Unauthorized(null));
    assertDoesNotThrow(() -> new Unauthorized(new RuntimeException()));
    assertThrows(NullPointerException.class, () -> new ServerError(null));
    assertDoesNotThrow(() -> new ServerError(new RuntimeException()));
    assertThrows(NullPointerException.class, () -> new BadRequest(null));
    assertDoesNotThrow(() -> new BadRequest(new RuntimeException()));
    assertThrows(NullPointerException.class, () -> new EntityNotFound(null));
    assertDoesNotThrow(() -> new EntityNotFound(new RuntimeException()));
  }
}
