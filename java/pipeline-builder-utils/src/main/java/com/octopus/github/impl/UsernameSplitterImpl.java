package com.octopus.github.impl;

import com.octopus.github.UsernameSplitter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * And implementation of UsernameSplitter.
 */
public class UsernameSplitterImpl implements UsernameSplitter {

  /**
   * Get the first name.
   *
   * @param username The combined name.
   * @return the first name.
   */
  @Override
  public String getFirstName(final String username) {
    if (StringUtils.isBlank(username)) {
      return "";
    }

    return Arrays.stream(username.split(" ")).findFirst().orElse("");
  }

  /**
   * Get the last name.
   *
   * @param username The combined name.
   * @return the last name.
   */
  @Override
  public String getLastName(final String username) {
    if (StringUtils.isBlank(username)) {
      return "";
    }

    final List<String> splitName = new ArrayList<>(List.of(username.split(" ")));
    if (splitName.size() < 2) {
      return "";
    }

    Collections.reverse(splitName);
    return splitName.stream().findFirst().orElse("");
  }
}
