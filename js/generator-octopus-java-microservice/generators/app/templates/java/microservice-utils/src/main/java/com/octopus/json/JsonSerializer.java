package com.octopus.json;

/**
 * Represents a JSON serializer.
 */
public interface JsonSerializer {

  /**
   * Convert the supplied object to JSON.
   *
   * @param object The object to be serialized.
   * @return The JSON representation.
   */
  String toJson(Object object);
}
