package com.octopus.loginmessage.infrastructure.octofront.impl;

import com.google.common.collect.ImmutableMap;
import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class CommercialAzureServiceBusEmptyTestProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return ImmutableMap.<String, String>builder()
        .put("commercial.servicebus.topic", "")
        .put("commercial.servicebus.secret", "")
        .put("commercial.servicebus.namespace", "")
        .put("commercial.servicebus.app-id", "")
        .put("commercial.servicebus.tenant", "")
        .put("commercial.servicebus.disabled", "true")
        .build();
  }

}
