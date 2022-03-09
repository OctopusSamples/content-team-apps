package com.octopus.githubrepo.domain.utils.impl;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.githubrepo.domain.entities.CreateGithubRepo;
import com.octopus.githubrepo.domain.entities.GenerateTemplate;

/**
 * A concrete implementation for the CreateServiceAccount type.
 */
public class JsonApiServiceUtilsGenerateTemplate extends JsonApiResourceUtilsImpl<GenerateTemplate> {
  /**
   * Constructor.
   *
   * @param resourceConverter The resource converted used to handle JSON documents.
   */
  public JsonApiServiceUtilsGenerateTemplate(final ResourceConverter resourceConverter) {
    super(resourceConverter);
  }
}
