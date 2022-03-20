package com.octopus.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonSerializationExceptionTest {


  @Test
  public void verifyValidInputs() {
    Assertions.assertDoesNotThrow(() -> new JsonSerializationException());
    Assertions.assertDoesNotThrow(() -> new JsonSerializationException("hi"));
    Assertions.assertDoesNotThrow(() -> new JsonSerializationException(new Exception()));
    Assertions.assertDoesNotThrow(() -> new JsonSerializationException((Throwable) null));
    Assertions.assertDoesNotThrow(() -> new JsonSerializationException((String) null));
  }
}
