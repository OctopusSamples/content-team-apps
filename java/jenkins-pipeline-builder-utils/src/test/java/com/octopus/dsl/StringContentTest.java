package com.octopus.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StringContentTest {

  @Test
  public void testComment() {
    final Element element = StringContent.builder().content("a string").build();
    assertEquals("  a string", element.toString());
  }
}
