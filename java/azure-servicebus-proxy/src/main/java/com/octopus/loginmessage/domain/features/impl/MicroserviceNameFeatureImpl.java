package com.octopus.loginmessage.domain.features.impl;

import com.octopus.features.MicroserviceNameFeature;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * The microservice name feature, sourced from the config file.
 */
@ApplicationScoped
public class MicroserviceNameFeatureImpl implements MicroserviceNameFeature {

  @ConfigProperty(name = "microservice.name")
  String microserviceName;

  @Override
  public String getMicroserviceName() {
    return microserviceName;
  }
}
