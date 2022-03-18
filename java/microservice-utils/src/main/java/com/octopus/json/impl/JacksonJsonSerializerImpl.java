package com.octopus.json.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octopus.exceptions.JsonSerializationException;
import com.octopus.json.JsonSerializer;
import lombok.NonNull;

/**
 * An implementation of JsonSerializer with Jackson.
 */
public class JacksonJsonSerializerImpl implements JsonSerializer {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public String toJson(@NonNull final Object object) {
    try {
      return OBJECT_MAPPER.writeValueAsString(object);
    } catch (final JsonProcessingException e) {
      throw new JsonSerializationException(e);
    }
  }
}
