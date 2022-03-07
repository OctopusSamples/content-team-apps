package com.octopus.githubrepo.domain.utils.impl;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.githubrepo.domain.entities.CreateGithubRepo;

/**
 * A concrete implementation for the CreateServiceAccount type.
 */
public class JsonApiServiceUtils extends JsonApiResourceUtilsImpl<CreateGithubRepo> {
  /**
   * Constructor.
   *
   * @param resourceConverter The resource converted used to handle JSON documents.
   */
  public JsonApiServiceUtils(ResourceConverter resourceConverter) {
    super(resourceConverter);
  }
}
