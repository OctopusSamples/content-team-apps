package com.octopus.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UnauthorizedExceptionTest {

  @Test
  public void verifyValidInputs() {
    Assertions.assertDoesNotThrow(() -> new UnauthorizedException());
    Assertions.assertDoesNotThrow(() -> new UnauthorizedException("hi"));
    Assertions.assertDoesNotThrow(() -> new UnauthorizedException(new Exception()));
    Assertions.assertDoesNotThrow(() -> new UnauthorizedException((Throwable) null));
    Assertions.assertDoesNotThrow(() -> new UnauthorizedException((String) null));
    Assertions.assertDoesNotThrow(() -> new UnauthorizedException((String) null, new Exception()));
    Assertions.assertDoesNotThrow(() -> new UnauthorizedException("hi", null));
    Assertions.assertDoesNotThrow(() -> new UnauthorizedException("hi", new Exception()));
  }
}
