package com.octopus.githubactions.github.domain;

import com.google.common.collect.ImmutableMap;
import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class MinimumTestingProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return ImmutableMap.<String, String>builder()
        .put("github.encryption", "12345678901234567890123456789012")
        .put("github.salt", "12345678901234567890123456789012")
        .build();
  }
}