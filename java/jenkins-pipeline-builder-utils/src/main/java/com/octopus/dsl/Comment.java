package com.octopus.dsl;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Represents a comment in the pipeline.
 */
@Getter
@SuperBuilder
public class Comment extends Element {

  private String content;

  /**
   * Returns the content as a comment.
   *
   * @return The groovy comment
   */
  public String toString() {
    return Arrays.stream(content.split("\n")).map(c -> getIndent() + "// " + c)
        .collect(Collectors.joining("\n"));
  }
}
