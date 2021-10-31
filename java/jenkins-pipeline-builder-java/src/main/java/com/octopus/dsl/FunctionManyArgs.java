package com.octopus.dsl;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Represents a function with many arguments.
 */
@Getter
@SuperBuilder
public class FunctionManyArgs extends ElementWithChildren {
  private static final int MAX_ARGS_SINGLE_LINE = 4;
  private List<Argument> args;

  /**
   * Builds a function with many arguments.
   *
   * @return The groovy function.
   */
  public String toString() {
    final List<Element> safeChildren = getSafeChildren();
    safeChildren.forEach(c -> c.parent = this);

    final String delimiter = args.size() > MAX_ARGS_SINGLE_LINE ? ",\n" + getIndent() + "  " : ", ";

    return getIndent() + name + "("
        + args.stream().map(Argument::toString)
        .filter(c -> !c.isBlank())
        .collect(Collectors.joining(delimiter)) + ")";
  }
}
