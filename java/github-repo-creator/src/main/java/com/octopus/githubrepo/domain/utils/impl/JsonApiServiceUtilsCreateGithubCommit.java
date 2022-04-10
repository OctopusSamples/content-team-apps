package com.octopus.githubrepo.domain.utils.impl;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.githubrepo.domain.entities.CreateGithubCommit;
import com.octopus.githubrepo.domain.entities.PopulateGithubRepo;

/**
 * A concrete implementation for the CreateServiceAccount type.
 */
public class JsonApiServiceUtilsCreateGithubCommit extends JsonApiResourceUtilsImpl<CreateGithubCommit> {
  /**
   * Constructor.
   *
   * @param resourceConverter The resource converted used to handle JSON documents.
   */
  public JsonApiServiceUtilsCreateGithubCommit(final ResourceConverter resourceConverter) {
    super(resourceConverter);
  }
}
