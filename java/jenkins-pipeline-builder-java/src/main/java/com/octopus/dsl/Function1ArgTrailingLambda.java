package com.octopus.dsl;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Represents a function with one argument and a trailing lambda.
 */
@Getter
@SuperBuilder
public class Function1ArgTrailingLambda extends FunctionTrailingLambda {

  private String arg;

  /**
   * Builds a function with an argument and trailing lambda containing the children.
   *
   * @return The groovy function.
   */
  public String toString() {
    final List<Element> safeChildren = getSafeChildren();
    safeChildren.forEach(c -> c.parent = this);

    return getIndent() + name + "('" + arg + "') {\n"
        + safeChildren
        .stream()
        .map(Object::toString)
        .filter(c -> !c.isBlank())
        .collect(Collectors.joining("\n"))
        + "\n" + getIndent() + "}";
  }
}
