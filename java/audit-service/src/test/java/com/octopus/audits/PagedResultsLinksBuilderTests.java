package com.octopus.audits;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.octopus.audits.domain.PagedResultsLinksBuilder;
import com.octopus.audits.domain.entities.Audit;
import com.octopus.audits.domain.wrappers.FilteredResultWrapper;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PagedResultsLinksBuilderTests {
  @Test
  public void testLinksAreAdded() {
    final List<Audit> resourceList = List.of(new Audit());
    final JSONAPIDocument<List<Audit>> document = new JSONAPIDocument<List<Audit>>(resourceList);
    final FilteredResultWrapper results = new FilteredResultWrapper(resourceList, 1l);
    PagedResultsLinksBuilder.generatePageLinks(document, "10", "10", results);

    assertTrue(document.getLinks().getLinks().entrySet().stream().anyMatch(e -> "first".equals(e.getKey())));
    assertTrue(document.getLinks().getLinks().entrySet().stream().anyMatch(e -> "first".equals(e.getKey())));
    assertTrue(document.getLinks().getFirst().getMeta().containsKey("total"));
    assertTrue(document.getLinks().getLast().getMeta().containsKey("total"));
  }
}
