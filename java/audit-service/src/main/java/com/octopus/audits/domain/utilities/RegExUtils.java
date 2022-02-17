package com.octopus.audits.domain.utilities;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;

/**
 * Utility methods for working with regexes.
 */
public class RegExUtils {
  /**
   * Get the regex group from the pattern for the input.
   *
   * @param pattern The regex pattern.
   * @param input The input to apply the pattern to.
   * @param group The group name to return.
   * @return The regex group value.
   */
  public static Optional<String> getGroup(
      @NonNull final Pattern pattern, final Object input, @NonNull final String group) {
    if (input == null) {
      return Optional.empty();
    }

    final Matcher matcher = pattern.matcher(input.toString());

    try {
      if (matcher.find()) {
        return Optional.of(matcher.group(group));
      }
    } catch (final IllegalArgumentException ex) {
      // ignored
    }

    return Optional.empty();
  }
}
