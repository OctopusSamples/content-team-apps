package com.octopus.customers.domain.framework.producers;

import com.github.jasminb.jsonapi.DeserializationFeature;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.customers.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.customers.domain.entities.Health;
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
    final ResourceConverter resourceConverter = new ResourceConverter(
        GithubUserLoggedInForFreeToolsEventV1.class, Health.class);
    resourceConverter.disableDeserializationOption(DeserializationFeature.REQUIRE_RESOURCE_ID);
    resourceConverter.enableDeserializationOption(DeserializationFeature.ALLOW_UNKNOWN_INCLUSIONS);
    return resourceConverter;
  }
}
