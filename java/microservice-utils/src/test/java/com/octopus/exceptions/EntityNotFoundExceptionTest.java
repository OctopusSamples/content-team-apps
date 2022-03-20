package com.octopus.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EntityNotFoundExceptionTest {

  @Test
  public void verifyValidInputs() {
    Assertions.assertDoesNotThrow(() -> new EntityNotFoundException());
    Assertions.assertDoesNotThrow(() -> new EntityNotFoundException("hi"));
    Assertions.assertDoesNotThrow(() -> new EntityNotFoundException(new Exception()));
    Assertions.assertDoesNotThrow(() -> new EntityNotFoundException((Throwable) null));
    Assertions.assertDoesNotThrow(() -> new EntityNotFoundException((String) null));
    Assertions.assertDoesNotThrow(() -> new EntityNotFoundException((String) null, new Exception()));
    Assertions.assertDoesNotThrow(() -> new EntityNotFoundException("hi", null));
    Assertions.assertDoesNotThrow(() -> new EntityNotFoundException("hi", new Exception()));
  }
}
