package com.octopus.github;

/**
 * A service to split a string representing a name into first name and last name.
 */
public interface UsernameSplitter {

  /**
   * Get the first name.
   *
   * @param username The combined name.
   * @return the first name.
   */
  String getFirstName(String username);

  /**
   * Get the last name.
   *
   * @param username The combined name.
   * @return the last name.
   */
  String getLastName(String username);
}
