package com.octopus.jsonapi;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.octopus.jsonapi.impl.PagedResultsLinksBuilderImpl;
import com.octopus.wrappers.FilteredResultWrapper;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PagedResultsLinksBuilderTests {

  private static final PagedResultsLinksBuilder PAGED_RESULTS_LINKS_BUILDER = new PagedResultsLinksBuilderImpl();

  @Test
  public void testLinksAreAdded() {
    final List<TestEntity> resourceList = List.of(new TestEntity(), new TestEntity(), new TestEntity(), new TestEntity(),
        new TestEntity(), new TestEntity(), new TestEntity(), new TestEntity(), new TestEntity(), new TestEntity());
    final JSONAPIDocument<List<TestEntity>> document = new JSONAPIDocument<List<TestEntity>>(resourceList);
    final FilteredResultWrapper results = new FilteredResultWrapper(resourceList, 100l);
    PAGED_RESULTS_LINKS_BUILDER.generatePageLinks(document, "5", "3", results, "resource");

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
    final List<TestEntity> resourceList = List.of(new TestEntity());
    final JSONAPIDocument<List<TestEntity>> document = new JSONAPIDocument<List<TestEntity>>(resourceList);
    final FilteredResultWrapper results = new FilteredResultWrapper(resourceList, 1l);
    assertThrows(NullPointerException.class, () -> {
      PAGED_RESULTS_LINKS_BUILDER.generatePageLinks(null, "10", "10", results, "resource");
    });
    assertThrows(NullPointerException.class, () -> {
      PAGED_RESULTS_LINKS_BUILDER.generatePageLinks(document, "10", "10", null, "resource");
    });
  }

  private class TestEntity {
    public Integer id;
  }
}
