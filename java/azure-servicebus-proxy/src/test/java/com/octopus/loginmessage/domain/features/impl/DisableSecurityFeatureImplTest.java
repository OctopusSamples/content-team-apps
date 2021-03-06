package com.octopus.loginmessage.domain.features.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.octopus.features.DisableSecurityFeature;
import com.octopus.loginmessage.CommercialAzureServiceBusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(CommercialAzureServiceBusTestProfile.class)
public class DisableSecurityFeatureImplTest {
  @Inject
  DisableSecurityFeature disableSecurityFeature;

  @Test
  public void verifyCognitoFields() {
    assertFalse(disableSecurityFeature.getCognitoAuthDisabled());
  }
}
