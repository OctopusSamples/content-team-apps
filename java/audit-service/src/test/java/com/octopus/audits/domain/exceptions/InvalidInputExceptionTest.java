package com.octopus.audits.domain.exceptions;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class InvalidInputExceptionTest {
  @Test
  public void verifyNullInputs() {
    assertThrows(NullPointerException.class, () -> {
      new InvalidInput(null);
    });
  }
}
