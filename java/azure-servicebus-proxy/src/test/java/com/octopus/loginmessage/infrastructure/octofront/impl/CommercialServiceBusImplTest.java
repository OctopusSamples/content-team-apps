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
 * This test ensures the message generated for the service bus has the required properties as documented
 * in https://github.com/OctopusDeploy/Architecture/blob/main/OctopusHQ/Strategy.md. However, the
 * service does not send a message because the settings are not defined that allow a
 * ServiceBusSenderClient to be created.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(CommercialAzureServiceBusEmptyTestProfile.class)
public class CommercialServiceBusImplTest {

  @Inject
  CommercialServiceBusImpl commercialServiceBus;

  @Test
  public void testGenerateMessage() {
    final ServiceBusMessage message = commercialServiceBus.generateMessage("trace", "body");
    assertTrue(StringUtils.isNotBlank(message.getApplicationProperties().get("context").toString()));
    assertTrue(StringUtils.isNotBlank(message.getApplicationProperties().get("source").toString()));
    assertTrue(StringUtils.isNotBlank(message.getApplicationProperties().get("type").toString()));
    assertTrue(StringUtils.isNotBlank(message.getApplicationProperties().get("occurredTimeUtc").toString()));
    assertEquals("body", message.getBody().toString());
    assertEquals("trace", message.getApplicationProperties().get("activityId").toString());
    assertEquals("1", message.getApplicationProperties().get("specVersion").toString());
  }

  @Test
  public void testInvalidArgs() {
    assertThrows(NullPointerException.class, () -> commercialServiceBus.sendUserDetails("", null));
    assertThrows(IllegalArgumentException.class, () -> commercialServiceBus.sendUserDetails("", ""));
  }

  @Test
  public void testSend() {
    assertDoesNotThrow(() -> commercialServiceBus.sendUserDetails("", "body"));
    assertDoesNotThrow(() -> commercialServiceBus.sendUserDetails(null, "body"));
    assertDoesNotThrow(() -> commercialServiceBus.sendUserDetails("traceid", "body"));
  }
}
