package com.octopus.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

public class FunctionManyArgsTest {

  @Test
  public void testFunctionManyArgs() {
    final Element element = FunctionManyArgs.builder()
        .name("function")
        .args(new ImmutableList.Builder<Argument>()
            .add(new Argument("name", "value", ArgType.STRING))
            .build())
        .build();
    assertEquals("function(name: 'value')", element.toString());
  }

  @Test
  public void testFunctionManyArgsMultiLine() {
    final Element element = FunctionManyArgs.builder()
        .name("function")
        .args(new ImmutableList.Builder<Argument>()
            .add(new Argument("name", "value", ArgType.STRING))
            .add(new Argument("name2", "value", ArgType.STRING))
            .add(new Argument("name3", "value", ArgType.STRING))
            .add(new Argument("name4", "value", ArgType.STRING))
            .add(new Argument("name5", "value", ArgType.STRING))
            .build())
        .build();
    assertEquals("function(name: 'value',\n  name2: 'value',\n  name3: 'value',\n  name4: 'value',\n  name5: 'value')", element.toString());
  }
}
