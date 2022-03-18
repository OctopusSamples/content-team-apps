package com.octopus.jsonapi.impl;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.Link;
import com.google.common.collect.ImmutableMap;
import com.octopus.Constants;
import com.octopus.jsonapi.PagedResultsLinksBuilder;
import com.octopus.wrappers.FilteredResultWrapper;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Utility methods for adding links and metadata to pages results.
 */
public class PagedResultsLinksBuilderImpl implements PagedResultsLinksBuilder {

  @Override
  public <T> void generatePageLinks(
      @NonNull final JSONAPIDocument<List<T>> document,
      final String pageLimit,
      final String pageOffset,
      @NonNull final FilteredResultWrapper<T> resources,
      @NonNull final String resourceName) {

    final int pageLimitParsed = NumberUtils.toInt(pageLimit, Constants.DEFAULT_PAGE_LIMIT);
    final int pageOffsetParsed = NumberUtils.toInt(pageOffset, Constants.DEFAULT_PAGE_OFFSET);
    final long lastOffset = Math.max(resources.getCount() - pageLimitParsed, 0);

    // See https://jsonapi.org/format/#document-links for an example of link metadata including a count
    final Map<String, Long> linkMeta = new ImmutableMap.Builder<String, Long>()
        .put("total",  resources.getCount())
        .build();

    document.addLink("first", new Link("/api/" + resourceName + "?page[offset]=0&page[limit]=" + pageLimitParsed, linkMeta));
    document.addLink("last", new Link("/api/" + resourceName + "?page[offset]=" + lastOffset + "&page[limit]=" + pageLimitParsed, linkMeta));

    if (lastOffset > pageOffsetParsed) {
      document.addLink("next", new Link(
          "/api/" + resourceName + "?page[offset]=" + Math.min(resources.getCount() - pageLimitParsed, pageOffsetParsed + pageLimitParsed) + "&page[limit]="
              + pageLimit,
          linkMeta));
    }

    if (pageOffsetParsed > 0) {
      document.addLink("prev", new Link(
          "/api/" + resourceName + "?page[offset]=" + Math.max(0, pageOffsetParsed - pageLimitParsed) + "&page[limit]="
              + pageLimit,
          linkMeta));
    }
  }
}
