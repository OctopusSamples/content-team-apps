package com.octopus.loginmessage.domain.framework.producers;

import static org.junit.jupiter.api.Assertions.assertNull;

import com.azure.messaging.servicebus.ServiceBusSenderClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * This test verifies the service bus client is null if required settings are missing.
 */
@QuarkusTest
@TestProfile(NullTenantProfile.class)
public class NullServiceBusTenantUtilityProducerTest {

  @Inject
  ServiceBusSenderClient serviceBusSenderClient;


  @Test
  public void injectionsShouldBeNull() {
    assertNull(serviceBusSenderClient);
  }
}
