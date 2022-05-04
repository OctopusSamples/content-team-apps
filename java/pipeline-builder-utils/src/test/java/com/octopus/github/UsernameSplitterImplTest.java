package com.octopus.github;

import com.octopus.github.impl.UsernameSplitterImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class UsernameSplitterImplTest {
  private static final UsernameSplitter USERNAME_SPLITTER = new UsernameSplitterImpl();

  @ParameterizedTest
  @CsvSource({
      "john doe,john,doe",
      "john  doe,john,doe",
      "john h doe,john,doe",
      "john a b c doe,john,doe",
      " john  doe ,john,doe",
      "john,john,"
  })
  public void regexTests(final String combined, final String first, final String last) {
    Assertions.assertEquals(first == null ? "" : first, USERNAME_SPLITTER.getFirstName(combined));
    Assertions.assertEquals(last == null ? "" : last, USERNAME_SPLITTER.getLastName(combined));
  }
}
