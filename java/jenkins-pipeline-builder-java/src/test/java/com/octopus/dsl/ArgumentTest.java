package com.octopus.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ArgumentTest {

  @Test
  public void testStringArgument() {
    final Argument argument = new Argument("name", "value", ArgType.STRING);
    assertEquals("name: 'value'", argument.toString());
  }

  @Test
  public void testNumberArgument() {
    final Argument argument = new Argument("name", "1", ArgType.NUMBER);
    assertEquals("name: 1", argument.toString());
  }

  @Test
  public void testBooleanArgument() {
    final Argument argument = new Argument("name", "true", ArgType.BOOLEAN);
    assertEquals("name: true", argument.toString());
  }

  @Test
  public void testArrayArgument() {
    final Argument argument = new Argument("name", "['hi']", ArgType.ARRAY);
    assertEquals("name: ['hi']", argument.toString());
  }

  @Test
  public void testTypeArgument() {
    final Argument argument = new Argument("name", "type", ArgType.TYPE);
    assertEquals("name: type", argument.toString());
  }
}
