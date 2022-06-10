package com.octopus.githubrepo;

import com.google.common.collect.ImmutableMap;
import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class TestingProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return ImmutableMap.<String, String>builder()
        .put("github.encryption", "12345678901234567890123456789012")
        .put("github.salt", "12345678901234567890123456789012")
        .put("client.private-key-base64", "blah")
        .put("quarkus.lambda.handler", "PopulateGithubRepo")
        .put("cognito.client-id", "cognito-id")
        .put("cognito.client-secret", "cognito-secret")
        .build();
  }
}