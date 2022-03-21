package com.octopus.loginmessage.domain.servicebus.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.loginmessage.domain.servicebus.AzureServiceBus;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(CommercialAzureServiceBusTestProfile.class)
public class CommercialAzureServiceBusTest {

  @Inject
  AzureServiceBus azureServiceBus;


  @Test
  public void testCredentialsConstruction() {
    assertTrue(azureServiceBus.getCredentials().isPresent());
    assertTrue(azureServiceBus.getNamespace().isPresent());
    assertTrue(azureServiceBus.getTopic().isPresent());
  }
}
