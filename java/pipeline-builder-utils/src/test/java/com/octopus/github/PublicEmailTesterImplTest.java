package com.octopus.github;

import com.octopus.github.impl.PublicEmailTesterImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class PublicEmailTesterImplTest {
  private static final PublicEmailTester PUBLIC_EMAIL_TESTER = new PublicEmailTesterImpl();

  @ParameterizedTest
  @ValueSource(strings = {
      "a@a.com",
      "a@example.com",
      "  a@blah.com   "
  })
  public void testPublicEmails(final String email) {
    Assertions.assertTrue(PUBLIC_EMAIL_TESTER.isPublicEmail(email));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      " ",
      "a@users.noreply.github.com",
      "  a@users.noreply.github.com   "
  })
  public void testPrivateOrInvalidEmails(final String email) {
    Assertions.assertFalse(PUBLIC_EMAIL_TESTER.isPublicEmail(email));
  }
}
