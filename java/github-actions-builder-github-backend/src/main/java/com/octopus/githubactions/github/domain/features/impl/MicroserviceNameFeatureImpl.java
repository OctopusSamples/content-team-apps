package com.octopus.githubactions.github.domain.features.impl;

import com.octopus.features.MicroserviceNameFeature;
import javax.enterprise.context.ApplicationScoped;

/**
 * The microservice name feature, implemented as a hard coded string.
 */
@ApplicationScoped
public class MicroserviceNameFeatureImpl implements MicroserviceNameFeature {

  private static final String MICROSERVICE_NAME = "GithubActionWorkflowBuilder";

  @Override
  public String getMicroserviceName() {
    return MICROSERVICE_NAME;
  }
}
