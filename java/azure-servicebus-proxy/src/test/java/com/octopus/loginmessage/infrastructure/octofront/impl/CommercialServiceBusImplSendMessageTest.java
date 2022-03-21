package com.octopus.loginmessage.infrastructure.octofront.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.messaging.servicebus.ServiceBusMessage;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import javax.inject.Inject;
import org.apache.maven.shared.utils.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * This test takes the CommercialServiceBusImpl right to the point of trying to send a message, because
 * the settings required to build a ServiceBusSenderClient are defined in the profile.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(CommercialAzureServiceBusPopulatedTestProfile.class)
public class CommercialServiceBusImplSendMessageTest {

  @Inject
  CommercialServiceBusImpl commercialServiceBus;

  @Test
  public void testSend() {
    assertDoesNotThrow(() -> commercialServiceBus.sendUserDetails("", "body"));
    assertDoesNotThrow(() -> commercialServiceBus.sendUserDetails(null, "body"));
    assertDoesNotThrow(() -> commercialServiceBus.sendUserDetails("traceid", "body"));
  }
}
