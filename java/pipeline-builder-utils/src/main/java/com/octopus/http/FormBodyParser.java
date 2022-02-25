package com.octopus.http;

import java.util.Map;

/**
 * Represents a service that can parse form bodies.
 */
public interface FormBodyParser {
  Map<String, String> parseFormBody(String body);
}
