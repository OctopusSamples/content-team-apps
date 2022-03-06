package com.octopus.serviceaccount.domain.framework.producers;

import com.github.jasminb.jsonapi.DeserializationFeature;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.serviceaccount.domain.entities.ApiKey;
import com.octopus.serviceaccount.domain.entities.CreateServiceAccount;
import com.octopus.serviceaccount.domain.entities.ServiceAccount;
import com.octopus.serviceaccount.domain.entities.Health;
import com.octopus.serviceaccount.domain.entities.CreateSpace;
import com.octopus.serviceaccount.domain.entities.Space;
import javax.enterprise.inject.Produces;

/**
 * Produces a JSONAPI resource converter.
 */
public class JsonApiConverter {

  /**
   * Produces a ResourceConverter.
   *
   * @return The configured ResourceConverter.
   */
  @Produces
  public ResourceConverter buildResourceConverter() {
    final ResourceConverter resourceConverter = new ResourceConverter(
        ServiceAccount.class,
        CreateServiceAccount.class,
        Space.class,
        ApiKey.class,
        CreateSpace.class,
        Health.class);
    resourceConverter.disableDeserializationOption(DeserializationFeature.REQUIRE_RESOURCE_ID);
    resourceConverter.enableDeserializationOption(DeserializationFeature.ALLOW_UNKNOWN_INCLUSIONS);
    return resourceConverter;
  }
}
