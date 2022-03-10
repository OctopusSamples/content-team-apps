package com.octopus.http;

import java.time.temporal.TemporalUnit;

/**
 * Defines methods for generating dates used by cookies.
 */
public interface CookieDateUtils {

  /**
   * Generate a date relative to now.
   *
   * @param offset The amount of time to offset by.
   * @param unit The time unit that offset is measured in.
   * @return The date string in a format recognized by cookies.
   */
  String getRelativeExpiryDate(final int offset, final TemporalUnit unit);
}
