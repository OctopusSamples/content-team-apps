package com.octopus.audits.domain.jsonapi;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.Link;
import com.google.common.collect.ImmutableMap;
import com.octopus.audits.GlobalConstants;
import com.octopus.audits.domain.wrappers.FilteredResultWrapper;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Utility methods for adding links and metadata to pages results.
 */
public class PagedResultsLinksBuilder {

  /**
   * Generate the links field for a JSON API collection.
   *
   * @param document The document containing the filtered list.
   * @param pageLimit The request page limit.
   * @param pageOffset The requested page offset.
   * @param resources The list of filtered results.
   * @param <T> The resource type.
   */
  public static <T> void generatePageLinks(
      @NonNull final JSONAPIDocument<List<T>> document,
      final String pageLimit,
      final String pageOffset,
      @NonNull final FilteredResultWrapper<T> resources) {
    final int pageLimitParsed = NumberUtils.toInt(pageLimit, GlobalConstants.DEFAULT_PAGE_LIMIT);
    final int pageOffsetParsed = NumberUtils.toInt(pageOffset, GlobalConstants.DEFAULT_PAGE_OFFSET);
    final long lastOffset = Math.max(resources.getCount() - pageLimitParsed, 0);

    // See https://jsonapi.org/format/#document-links for an example of link metadata including a count
    final Map<String, Long> linkMeta = new ImmutableMap.Builder<String, Long>()
        .put("total",  resources.getCount())
        .build();

    document.addLink("first", new Link("/api/audits?page[offset]=0&page[limit]=" + pageLimitParsed, linkMeta));
    document.addLink("last", new Link("/api/audits?page[offset]=" + lastOffset + "&page[limit]=" + pageLimitParsed, linkMeta));

    if (lastOffset > pageOffsetParsed) {
      document.addLink("next", new Link(
          "/api/audits?page[offset]=" + Math.min(resources.getCount() - pageLimitParsed, pageOffsetParsed + pageLimitParsed) + "&page[limit]="
              + pageLimit,
          linkMeta));
    }

    if (pageOffsetParsed > 0) {
      document.addLink("prev", new Link(
          "/api/audits?page[offset]=" + Math.max(0, pageOffsetParsed - pageLimitParsed) + "&page[limit]="
              + pageLimit,
          linkMeta));
    }
  }
}
