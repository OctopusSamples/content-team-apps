package com.octopus.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EncryptionExceptionTest {

  @Test
  public void verifyValidInputs() {
    Assertions.assertDoesNotThrow(() -> new EncryptionException());
    Assertions.assertDoesNotThrow(() -> new EncryptionException("hi"));
    Assertions.assertDoesNotThrow(() -> new EncryptionException(new Exception()));
    Assertions.assertDoesNotThrow(() -> new EncryptionException((Throwable) null));
    Assertions.assertDoesNotThrow(() -> new EncryptionException((String) null));
  }
}
