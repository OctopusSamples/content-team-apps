package com.octopus.loginmessage.domain.features.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.features.AdminJwtGroupFeature;
import com.octopus.features.MicroserviceNameFeature;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import javax.inject.Inject;
import org.apache.maven.shared.utils.StringUtils;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(FeatureTestProfile.class)
public class MicroserviceNameFeatureImplTest {
  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  @Test
  public void verifyCognitoFields() {
    assertTrue(StringUtils.isNotBlank(microserviceNameFeature.getMicroserviceName()));
  }
}
