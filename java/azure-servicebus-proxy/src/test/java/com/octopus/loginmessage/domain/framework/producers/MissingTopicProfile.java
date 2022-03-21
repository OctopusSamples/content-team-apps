package com.octopus.loginmessage.domain.framework.producers;

import com.google.common.collect.ImmutableMap;
import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class MissingTopicProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return ImmutableMap.<String, String>builder()
        .put("commercial.servicebus.secret", "blah")
        .put("commercial.servicebus.app-id", "blah")
        .put("commercial.servicebus.namespace", "blah")
        .put("commercial.servicebus.tenant", "blah")
        .put("commercial.servicebus.topic", "")
        .build();
  }

}
