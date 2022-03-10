package com.octopus.jsonapi;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.octopus.wrappers.FilteredResultWrapper;
import java.util.List;
import lombok.NonNull;

/**
 * A service used to build paged links in JSONAPI responses.
 */
public interface PagedResultsLinksBuilder {

  /**
   * Generate the links field for a JSON API collection.
   *
   * @param document   The document containing the filtered list.
   * @param pageLimit  The request page limit.
   * @param pageOffset The requested page offset.
   * @param resources  The list of filtered results.
   * @param <T>        The resource type.
   */
  <T> void generatePageLinks(
      JSONAPIDocument<List<T>> document,
      String pageLimit,
      String pageOffset,
      FilteredResultWrapper<T> resources);
}
