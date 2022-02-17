package com.octopus.audits.domain.jsonapi;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.octopus.audits.domain.jsonapi.PagedResultsLinksBuilder;
import com.octopus.audits.domain.entities.Audit;
import com.octopus.audits.domain.wrappers.FilteredResultWrapper;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PagedResultsLinksBuilderTests {

  @Test
  public void testLinksAreAdded() {
    final List<Audit> resourceList = List.of(new Audit(), new Audit(), new Audit(), new Audit(),
        new Audit(), new Audit(), new Audit(), new Audit(), new Audit(), new Audit());
    final JSONAPIDocument<List<Audit>> document = new JSONAPIDocument<List<Audit>>(resourceList);
    final FilteredResultWrapper results = new FilteredResultWrapper(resourceList, 100l);
    PagedResultsLinksBuilder.generatePageLinks(document, "5", "3", results);

    assertTrue(document.getLinks().getLinks().entrySet().stream()
        .anyMatch(e -> "first".equals(e.getKey())));
    assertTrue(document.getLinks().getLinks().entrySet().stream()
        .anyMatch(e -> "first".equals(e.getKey())));
    assertTrue(document.getLinks().getLinks().entrySet().stream()
        .anyMatch(e -> "next".equals(e.getKey())));
    assertTrue(document.getLinks().getLinks().entrySet().stream()
        .anyMatch(e -> "prev".equals(e.getKey())));
    assertTrue(document.getLinks().getFirst().getMeta().containsKey("total"));
    assertTrue(document.getLinks().getLast().getMeta().containsKey("total"));
    assertTrue(document.getLinks().getNext().getMeta().containsKey("total"));
    assertTrue(document.getLinks().getPrevious().getMeta().containsKey("total"));
  }

  @Test
  public void verifyNullInputs() {
    final List<Audit> resourceList = List.of(new Audit());
    final JSONAPIDocument<List<Audit>> document = new JSONAPIDocument<List<Audit>>(resourceList);
    final FilteredResultWrapper results = new FilteredResultWrapper(resourceList, 1l);
    assertThrows(NullPointerException.class, () -> {
      PagedResultsLinksBuilder.generatePageLinks(null, "10", "10", results);
    });
    assertThrows(NullPointerException.class, () -> {
      PagedResultsLinksBuilder.generatePageLinks(document, "10", "10", null);
    });
  }
}
