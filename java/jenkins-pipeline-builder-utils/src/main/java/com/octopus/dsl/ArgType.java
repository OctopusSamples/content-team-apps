package com.octopus.dsl;

/**
 * Defines the type of an argument when building a groovy function call.
 */
public enum ArgType {
  STRING,
  EXPANDED_STRING,
  BOOLEAN,
  NUMBER,
  TYPE,
  ARRAY,
  CODE
}
