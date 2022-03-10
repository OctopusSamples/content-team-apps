package com.octopus.utilties.impl;

import com.octopus.utilties.RegExUtils;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;

/**
 * Utility methods for working with regexes.
 */
public class RegExUtilsImpl implements RegExUtils {
  @Override
  public Optional<String> getGroup(
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
