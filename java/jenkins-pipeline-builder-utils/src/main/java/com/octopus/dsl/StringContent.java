package com.octopus.dsl;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Represents plain string content, such as the content of a script block.
 */
@Getter
@SuperBuilder
public class StringContent extends Element {

  private String content;

  /**
   * Returns the content as plain, indented string.
   *
   * @return The groovy content
   */
  public String toString() {
    return getIndent() + "  " + Arrays.stream(content.split("\n"))
        .collect(Collectors.joining("\n" + getIndent() + "  "));
  }
}
