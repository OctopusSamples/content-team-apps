package com.octopus.customers.domain.features;

import com.octopus.features.MicroserviceNameFeature;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MicroserviceNameFeatureImpl implements MicroserviceNameFeature {

  @Override
  public String getMicroserviceName() {
    return "CustomerService";
  }
}
