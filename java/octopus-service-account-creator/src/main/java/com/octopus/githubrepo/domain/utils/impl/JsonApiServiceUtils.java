package com.octopus.githubrepo.domain.utils.impl;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.githubrepo.domain.entities.CreateServiceAccount;

/**
 * A concrete implementation for the CreateServiceAccount type.
 */
public class JsonApiServiceUtils extends JsonApiResourceUtilsImpl<CreateServiceAccount> {
  /**
   * Constructor.
   *
   * @param resourceConverter The resource converted used to handle JSON documents.
   */
  public JsonApiServiceUtils(ResourceConverter resourceConverter) {
    super(resourceConverter);
  }
}
