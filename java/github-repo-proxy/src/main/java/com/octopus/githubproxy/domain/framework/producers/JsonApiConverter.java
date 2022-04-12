package com.octopus.githubproxy.domain.framework.producers;

import com.github.jasminb.jsonapi.DeserializationFeature;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.githubproxy.domain.entities.GitHubRepo;
import com.octopus.githubproxy.domain.entities.GitHubRepoMeta;
import com.octopus.githubproxy.domain.entities.Health;
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
    final ResourceConverter resourceConverter = new ResourceConverter(GitHubRepo.class, Health.class, GitHubRepoMeta.class);
    resourceConverter.disableDeserializationOption(DeserializationFeature.REQUIRE_RESOURCE_ID);
    resourceConverter.enableDeserializationOption(DeserializationFeature.ALLOW_UNKNOWN_INCLUSIONS);
    return resourceConverter;
  }
}
