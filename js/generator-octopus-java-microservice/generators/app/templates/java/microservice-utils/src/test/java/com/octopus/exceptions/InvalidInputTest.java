package com.octopus.exceptions;

import com.octopus.exceptions.InvalidInput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InvalidInputTest {
  @Test
  public void verifyNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      new InvalidInput(null);
    });
  }
}
