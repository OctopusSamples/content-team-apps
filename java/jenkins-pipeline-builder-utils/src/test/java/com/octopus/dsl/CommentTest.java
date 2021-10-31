package com.octopus.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void testComment() {
    final Comment element = Comment.builder().content("a comment").build();
    assertEquals("// a comment", element.toString());
  }

  @Test
  public void testMultilineComment() {
    final Comment element = Comment.builder().content("a comment\nsecond line").build();
    assertEquals("// a comment\n// second line", element.toString());
  }
}
