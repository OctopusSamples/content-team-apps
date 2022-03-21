package com.octopus.loginmessage.domain.features.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.MicroserviceNameFeature;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import javax.inject.Inject;
import org.apache.maven.shared.utils.StringUtils;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(FeatureTestProfile.class)
public class AdminJwtClaimFeatureImplTest {
  @Inject
  AdminJwtClaimFeature adminJwtClaimFeature;

  @Test
  public void verifyCognitoFields() {
    assertTrue(adminJwtClaimFeature.getAdminClaim().isPresent());
    assertTrue(StringUtils.isNotBlank(adminJwtClaimFeature.getAdminClaim().get()));
  }
}
