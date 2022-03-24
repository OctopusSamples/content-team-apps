package com.octopus.githubrepo.domain.utils.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LinksHeaderParsingImplTest {
  private static final LinksHeaderParsingImpl LINKS_HEADER_PARSING = new LinksHeaderParsingImpl();

  @Test
  public void testLinkParsing() {
    assertEquals("3", LINKS_HEADER_PARSING.getLastPage(
        "<https://api.github.com/repositories/473410322/commits?per_page=1&page=2>; rel=\"next\", <https://api.github.com/repositories/473410322/commits?per_page=1&page=3>; rel=\"last\"").get());
    assertEquals("3", LINKS_HEADER_PARSING.getLastPage(
        "<https://api.github.com/repositories/473410322/commits?per_page=1&page=2>; rel=\"next\", <https://api.github.com/repositories/473410322/commits?page=3&per_page=1>; rel=\"last\"").get());
    assertEquals("3", LINKS_HEADER_PARSING.getLastPage(
        "<https://api.github.com/repositories/473410322/commits?page=3&per_page=1>; rel=\"last\", <https://api.github.com/repositories/473410322/commits?per_page=1&page=2>; rel=\"next\"").get());
    assertEquals("3", LINKS_HEADER_PARSING.getLastPage(
        "<https://api.github.com/repositories/473410322/commits?page=3&per_page=1>; rel=\"last\" , <https://api.github.com/repositories/473410322/commits?per_page=1&page=2>; rel=\"next\"").get());
    assertTrue(LINKS_HEADER_PARSING.getLastPage("").isEmpty());
  }
}
