package com.octopus.utilties;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service for working with regexes.
 */
public interface RegExUtils {

  /**
   * Get the regex group from the pattern for the input.
   *
   * @param pattern The regex pattern.
   * @param input   The input to apply the pattern to.
   * @param group   The group name to return.
   * @return The regex group value.
   */
  Optional<String> getGroup(Pattern pattern, Object input, String group);
}
