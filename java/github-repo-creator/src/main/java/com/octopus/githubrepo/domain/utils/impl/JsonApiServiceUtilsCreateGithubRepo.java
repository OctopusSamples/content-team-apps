package com.octopus.githubrepo.domain.utils.impl;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.githubrepo.domain.entities.PopulateGithubRepo;

/**
 * A concrete implementation for the CreateServiceAccount type.
 */
public class JsonApiServiceUtilsCreateGithubRepo extends JsonApiResourceUtilsImpl<PopulateGithubRepo> {
  /**
   * Constructor.
   *
   * @param resourceConverter The resource converted used to handle JSON documents.
   */
  public JsonApiServiceUtilsCreateGithubRepo(final ResourceConverter resourceConverter) {
    super(resourceConverter);
  }
}
