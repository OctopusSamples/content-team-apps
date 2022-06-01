package com.octopus.audits.infrastructure.repositories;

import com.github.tennaito.rsql.misc.ArgumentFormatException;
import com.github.tennaito.rsql.misc.DefaultArgumentParser;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The DefaultArgumentParser does not understand Timestamps. This class adds the ability to parse
 * timestamp queries.
 */
public class CustomizedArgumentParser extends DefaultArgumentParser {

  @Override
  public <T> T parse(String argument, Class<T> type) throws ArgumentFormatException, IllegalArgumentException {
    if (type.isAssignableFrom(Timestamp.class)) {
      final ZonedDateTime parsed = ZonedDateTime.parse(argument, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      return (T) Timestamp.from(parsed.toInstant());
    }
    return super.parse(argument, type);
  }
}
