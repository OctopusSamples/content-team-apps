package com.octopus.dsl;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents an individual argument passed to a groovy function.
 */
@Data
@AllArgsConstructor
public class Argument {

  private String name;
  private String value;
  private ArgType type;

  /**
   * Convert the argument to a string.
   *
   * @return The string representation of the function argument
   */
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    if (!StringUtils.isBlank(name)) {
      builder.append(name);
      builder.append(": ");
    }
    if (type == ArgType.STRING) {
      builder.append("'");
    } else if (type == ArgType.EXPANDED_STRING) {
      builder.append("\"");
    }
    builder.append(value);
    if (type == ArgType.STRING) {
      builder.append("'");
    } else if (type == ArgType.EXPANDED_STRING) {
      builder.append("\"");
    }
    return builder.toString();
  }
}
