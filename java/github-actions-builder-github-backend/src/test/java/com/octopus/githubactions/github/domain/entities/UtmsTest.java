package com.octopus.githubactions.github.domain.entities;

import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Verify the UTM data class.
 */
public class UtmsTest {
  @Test
  public void testUtmsToMap() {
    final Map<String, String> map1 = Utms.builder()
        .campaign("campaign")
        .build()
        .getMap();
    assertTrue(map1.size() == 1);
    assertEquals("campaign", map1.get("utm_campaign"));

    final Map<String, String> map2 = Utms.builder()
        .content("content")
        .build()
        .getMap();
    assertTrue(map2.size() == 1);
    assertEquals("content", map2.get("utm_content"));

    final Map<String, String> map3 = Utms.builder()
        .medium("medium")
        .build()
        .getMap();
    assertTrue(map3.size() == 1);
    assertEquals("medium", map3.get("utm_medium"));

    final Map<String, String> map4 = Utms.builder()
        .source("source")
        .build()
        .getMap();
    assertTrue(map4.size() == 1);
    assertEquals("source", map4.get("utm_source"));

    final Map<String, String> map5 = Utms.builder()
        .term("term")
        .build()
        .getMap();
    assertTrue(map5.size() == 1);
    assertEquals("term", map5.get("utm_term"));
  }

  @Test
  public void testAllUtms() {
    final Map<String, String> map6 = Utms.builder()
        .term("term")
        .source("source")
        .medium("medium")
        .campaign("campaign")
        .content("content")
        .build()
        .getMap();
    assertTrue(map6.size() == 5);
  }
}
