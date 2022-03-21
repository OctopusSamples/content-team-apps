package com.octopus.loginmessage.domain.features.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.features.AdminJwtGroupFeature;
import com.octopus.loginmessage.CommercialAzureServiceBusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import javax.inject.Inject;
import org.apache.maven.shared.utils.StringUtils;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(CommercialAzureServiceBusTestProfile.class)
public class AdminJwtGroupFeatureImplTest {
  @Inject
  AdminJwtGroupFeature adminJwtGroupFeature;

  @Test
  public void verifyCognitoFields() {
    assertTrue(adminJwtGroupFeature.getAdminGroup().isPresent());
    assertTrue(StringUtils.isNotBlank(adminJwtGroupFeature.getAdminGroup().get()));
  }
}
