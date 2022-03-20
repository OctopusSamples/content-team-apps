package com.octopus.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InvalidInputExceptionTest {

  @Test
  public void verifyValidInputs() {
    Assertions.assertDoesNotThrow(() -> new InvalidInputException());
    Assertions.assertDoesNotThrow(() -> new InvalidInputException("hi"));
    Assertions.assertDoesNotThrow(() -> new InvalidInputException(new Exception()));
    Assertions.assertDoesNotThrow(() -> new InvalidInputException((Throwable) null));
    Assertions.assertDoesNotThrow(() -> new InvalidInputException((String) null));
  }
}
