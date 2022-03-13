package com.octopus.githubactions.domain.features.impl;

import com.octopus.features.MicroserviceNameFeature;
import com.octopus.githubactions.GlobalConstants;
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
