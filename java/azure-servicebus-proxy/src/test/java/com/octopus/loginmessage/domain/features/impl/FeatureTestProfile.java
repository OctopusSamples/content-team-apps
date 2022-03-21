package com.octopus.loginmessage.domain.features.impl;

import com.google.common.collect.ImmutableMap;
import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class FeatureTestProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return ImmutableMap.<String, String>builder()
        .put("cognito.jwk-base64", "blah")
        .put("cognito.disable-auth", "false")
        .put("cognito.admin-group", "groupname")
        .put("microservice.name", "name")
        .put("cognito.admin-claim", "claimname")
        .build();
  }

}
