package com.octopus.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServerErrorExceptionTest {
  @Test
  public void verifyValidInputs() {
    Assertions.assertDoesNotThrow(() -> new ServerErrorException());
    Assertions.assertDoesNotThrow(() -> new ServerErrorException("hi"));
    Assertions.assertDoesNotThrow(() -> new ServerErrorException(new Exception()));
    Assertions.assertDoesNotThrow(() -> new ServerErrorException((Throwable) null));
    Assertions.assertDoesNotThrow(() -> new ServerErrorException((String) null));
  }
}
