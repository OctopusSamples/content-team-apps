package com.octopus.jenkins.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import com.octopus.jenkins.shared.dsl.Comment;
import com.octopus.jenkins.shared.dsl.Element;
import com.octopus.jenkins.shared.dsl.Function1ArgTrailingLambda;
import org.junit.jupiter.api.Test;

public class Function1ArgTrailingLambdaTest {

  @Test
  public void testFunction1Arg() {
    final Element element = Function1ArgTrailingLambda.builder()
        .name("function")
        .arg("argument")
        .children(new ImmutableList.Builder<Element>()
            .add(Comment.builder().content("comment").build())
            .build())
        .build();
    assertEquals("function('argument') {\n  // comment\n}", element.toString());
  }
}
