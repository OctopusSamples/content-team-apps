package com.octopus.githuboauth;

import com.google.common.collect.ImmutableMap;
import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class OauthLoginLambaProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return ImmutableMap.<String, String>builder()
        .put("github.encryption", "12345678901234567890123456789012")
        .put("github.salt", "12345678901234567890123456789012")
        .put("github.client.redirect", "redirect")
        .put("github.client.id", "clientid")
        .put("github.client.secret", "secret")
        .put("quarkus.lambda.handler", "login")
        .put("github.login.redirect", "redirect")
        .build();
  }
}