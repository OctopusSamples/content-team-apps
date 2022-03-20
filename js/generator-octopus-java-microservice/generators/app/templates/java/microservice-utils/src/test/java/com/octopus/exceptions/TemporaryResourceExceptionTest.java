package com.octopus.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TemporaryResourceExceptionTest {

  @Test
  public void verifyValidInputs() {
    Assertions.assertDoesNotThrow(() -> new TemporaryResourceException());
    Assertions.assertDoesNotThrow(() -> new TemporaryResourceException("hi"));
    Assertions.assertDoesNotThrow(() -> new TemporaryResourceException(new Exception()));
    Assertions.assertDoesNotThrow(() -> new TemporaryResourceException((Throwable) null));
    Assertions.assertDoesNotThrow(() -> new TemporaryResourceException((String) null));
    Assertions.assertDoesNotThrow(() -> new TemporaryResourceException((String) null, new Exception()));
    Assertions.assertDoesNotThrow(() -> new TemporaryResourceException("hi", null));
    Assertions.assertDoesNotThrow(() -> new TemporaryResourceException("hi", new Exception()));
  }
}
