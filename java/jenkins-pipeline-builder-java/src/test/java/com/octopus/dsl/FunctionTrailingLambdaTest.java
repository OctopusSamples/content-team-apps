package com.octopus.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

public class FunctionTrailingLambdaTest {

  @Test
  public void testFunctionTrailingLambda() {
    final Element element = FunctionTrailingLambda.builder()
        .name("function")
        .children(new ImmutableList.Builder<Element>()
            .add(Comment.builder().content("comment").build())
            .build())
        .build();
    assertEquals("function {\n  // comment\n}", element.toString());
  }
}
