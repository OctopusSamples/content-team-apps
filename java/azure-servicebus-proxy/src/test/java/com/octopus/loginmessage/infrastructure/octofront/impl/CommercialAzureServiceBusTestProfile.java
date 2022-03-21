package com.octopus.loginmessage.infrastructure.octofront.impl;

import com.google.common.collect.ImmutableMap;
import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class CommercialAzureServiceBusTestProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return ImmutableMap.<String, String>builder()
        .put("commercial.servicebus.disabled", "true")
        .build();
  }

}
