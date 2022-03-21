package com.octopus.loginmessage.domain.servicebus.impl;

import com.google.common.collect.ImmutableMap;
import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class CommercialAzureServiceBusTestEmptyProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return ImmutableMap.<String, String>builder()
        .put("commercial.servicebus.topic", "")
        .put("commercial.servicebus.secret", "")
        .put("commercial.servicebus.app-id", "")
        .put("commercial.servicebus.namespace", "")
        .put("commercial.servicebus.tenant", "")
        .build();
  }

}
