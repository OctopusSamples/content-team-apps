package com.octopus.githubrepo.domain.features;

import com.octopus.features.MicroserviceNameFeature;
import javax.enterprise.context.ApplicationScoped;

/**
 * The microservice name feature, implemented as a hard coded string.
 */
@ApplicationScoped
public class MicroserviceNameFeatureImpl implements MicroserviceNameFeature {

  @Override
  public String getMicroserviceName() {
    return "GitHubRepo";
  }
}
