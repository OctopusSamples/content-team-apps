package com.octopus.audits.domain.framework.producers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.jasminb.jsonapi.DeserializationFeature;
import com.github.jasminb.jsonapi.Link;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.audits.domain.entities.Audit;
import com.octopus.audits.domain.entities.Health;
import com.octopus.audits.domain.framework.producers.JsonApiConverter.LinkAccessor.LinkAccessorSerializer;
import javax.enterprise.inject.Produces;

/** Produces a JSONAPI resource converter. */
public class JsonApiConverter {

  /**
   * Produces a ResourceConverter.
   *
   * @return The configured ResourceConverter.
   */
  @Produces
  public ResourceConverter buildResourceConverter() {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    final SimpleModule module = new SimpleModule();
    module.addSerializer(Link.class, new LinkAccessorSerializer());
    objectMapper.registerModule(module);

    final ResourceConverter resourceConverter = new ResourceConverter(objectMapper, Audit.class, Health.class);
    resourceConverter.disableDeserializationOption(DeserializationFeature.REQUIRE_RESOURCE_ID);
    resourceConverter.enableDeserializationOption(DeserializationFeature.ALLOW_UNKNOWN_INCLUSIONS);
    return resourceConverter;
  }

  protected static class LinkAccessor extends Link {
    protected static class LinkAccessorSerializer extends Link.LinkSerializer {

    }
  }
}
