package com.octopus.audits.domain.framework.producers;

import com.github.jasminb.jsonapi.DeserializationFeature;
import com.github.jasminb.jsonapi.Link;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.audits.domain.entities.Audit;
import com.octopus.audits.domain.entities.Health;
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
    final ResourceConverter resourceConverter =
        new ResourceConverter(Audit.class, Health.class);
    resourceConverter.disableDeserializationOption(DeserializationFeature.REQUIRE_RESOURCE_ID);
    resourceConverter.enableDeserializationOption(DeserializationFeature.ALLOW_UNKNOWN_INCLUSIONS);
    return resourceConverter;
  }
}
