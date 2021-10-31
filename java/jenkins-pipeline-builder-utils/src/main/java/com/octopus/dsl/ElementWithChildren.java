package com.octopus.dsl;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Represents a DSL element with children.
 */
@Getter
@SuperBuilder
public class ElementWithChildren extends Element {

  protected String name;
  private List<Element> children;

  protected List<Element> getSafeChildren() {
    return children == null ? List.of() : children;
  }
}
