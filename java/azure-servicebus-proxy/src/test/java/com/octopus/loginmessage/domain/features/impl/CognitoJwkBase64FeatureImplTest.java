package com.octopus.loginmessage.domain.features.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.features.CognitoJwkBase64Feature;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import javax.inject.Inject;
import org.apache.maven.shared.utils.StringUtils;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(FeatureTestProfile.class)
public class CognitoJwkBase64FeatureImplTest {
  @Inject
  CognitoJwkBase64Feature cognitoJwkBase64Feature;

  @Test
  public void verifyCognitoFields() {
      assertTrue(cognitoJwkBase64Feature.getCognitoJwk().isPresent());
      assertTrue(StringUtils.isNotBlank(cognitoJwkBase64Feature.getCognitoJwk().get()));
  }
}
