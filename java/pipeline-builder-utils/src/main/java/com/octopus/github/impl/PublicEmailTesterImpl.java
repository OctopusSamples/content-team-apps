package com.octopus.github.impl;

import com.octopus.github.PublicEmailTester;
import org.apache.commons.lang3.StringUtils;

/**
 * An implementation of PublicEmailTester.
 */
public class PublicEmailTesterImpl  implements PublicEmailTester {

  private static final String NO_REPLY_DOMAIN = "users.noreply.github.com";

  /**
   * Tests the email address to see if it is a public one.
   *
   * @param email The email address to test.
   * @return true if it is a public email, and false otherwise.
   */
  @Override
  public boolean isPublicEmail(final String email) {
    if (StringUtils.isBlank(email)) {
      return false;
    }

    if (email.trim().endsWith(NO_REPLY_DOMAIN)) {
      return false;
    }

    return true;
  }
}
