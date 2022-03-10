package com.octopus.http.impl;

import com.octopus.http.CookieDateUtils;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * An implementation of CookieDateUtils.
 */
public class CookieDateUtilsImpl implements CookieDateUtils {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss z").withZone(ZoneId.of("GMT"));

  /**
   * {@inheritDoc}
   */
  public String getRelativeExpiryDate(final int offset, final TemporalUnit unit) {
    final OffsetDateTime expiry = OffsetDateTime.now(ZoneOffset.UTC).plus(2, ChronoUnit.HOURS);
    return DATE_TIME_FORMATTER.format(expiry);
  }
}
