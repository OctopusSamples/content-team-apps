package com.octopus.github;

/**
 * Github has a habit of returning username@users.noreply.github.com email addresses for users.
 * These aren't useful to us, so this interface defines a service that detects public emails from
 * the noreply ones.
 */
public interface PublicEmailTester {

  /**
   * Tests the email address to see if it is a public one.
   *
   * @param email The email address to test.
   * @return true if it is a public email, and false otherwise.
   */
  boolean isPublicEmail(String email);
}
