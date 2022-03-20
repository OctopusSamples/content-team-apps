package com.octopus.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InvalidClientIdExceptionTest {

  @Test
  public void verifyValidInputs() {
    Assertions.assertDoesNotThrow(() -> new InvalidClientIdException());
    Assertions.assertDoesNotThrow(() -> new InvalidClientIdException("hi"));
    Assertions.assertDoesNotThrow(() -> new InvalidClientIdException(new Exception()));
    Assertions.assertDoesNotThrow(() -> new InvalidClientIdException((Throwable) null));
    Assertions.assertDoesNotThrow(() -> new InvalidClientIdException((String) null));
    Assertions.assertDoesNotThrow(() -> new InvalidClientIdException((String) null, new Exception()));
    Assertions.assertDoesNotThrow(() -> new InvalidClientIdException("hi", null));
    Assertions.assertDoesNotThrow(() -> new InvalidClientIdException("hi", new Exception()));
  }
}
