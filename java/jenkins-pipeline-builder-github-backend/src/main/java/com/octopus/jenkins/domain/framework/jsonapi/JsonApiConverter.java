package com.octopus.jenkins.domain.framework.jsonapi;

import com.github.jasminb.jsonapi.DeserializationFeature;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.jenkins.domain.entities.Audit;
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
        new ResourceConverter(Audit.class);
    resourceConverter.disableDeserializationOption(DeserializationFeature.REQUIRE_RESOURCE_ID);
    resourceConverter.enableDeserializationOption(DeserializationFeature.ALLOW_UNKNOWN_INCLUSIONS);
    return resourceConverter;
  }
}
