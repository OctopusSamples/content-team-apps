package com.octopus.githubrepo.domain.framework.producers;

import com.github.jasminb.jsonapi.DeserializationFeature;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.SerializationFeature;
import com.octopus.githubrepo.domain.entities.CreateGithubCommit;
import com.octopus.githubrepo.domain.entities.GenerateTemplate;
import com.octopus.githubrepo.domain.entities.Health;
import com.octopus.githubrepo.domain.entities.PopulateGithubRepo;
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
        PopulateGithubRepo.class,
        CreateGithubCommit.class,
        GenerateTemplate.class,
        Health.class);
    resourceConverter.disableDeserializationOption(DeserializationFeature.REQUIRE_RESOURCE_ID);
    resourceConverter.enableDeserializationOption(DeserializationFeature.ALLOW_UNKNOWN_INCLUSIONS);
    resourceConverter.enableSerializationOption(SerializationFeature.INCLUDE_RELATIONSHIP_ATTRIBUTES);
    return resourceConverter;
  }
}
