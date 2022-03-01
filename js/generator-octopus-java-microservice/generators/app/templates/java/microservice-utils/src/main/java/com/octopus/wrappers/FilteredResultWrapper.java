package com.octopus.wrappers;

import java.util.List;
import lombok.Data;

/**
 * Represents the results of a filtered collection, and the total number of available results.
 *
 * @param <T> The entity type.
 */
@Data
public class FilteredResultWrapper<T> {

  private List<T> list;
  private Long count;

  public FilteredResultWrapper() {

  }

  public FilteredResultWrapper(final List<T> list, final Long count) {
    this.list = list;
    this.count = count;
  }
}
