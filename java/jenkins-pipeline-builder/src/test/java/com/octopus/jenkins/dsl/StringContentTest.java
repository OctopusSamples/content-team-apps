package com.octopus.jenkins.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.octopus.jenkins.shared.dsl.Element;
import com.octopus.jenkins.shared.dsl.StringContent;
import org.junit.jupiter.api.Test;

public class StringContentTest {

  @Test
  public void testComment() {
    final Element element = StringContent.builder().content("a string").build();
    assertEquals("  a string", element.toString());
  }
}
