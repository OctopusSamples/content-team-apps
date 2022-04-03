package com.octopus.loginmessage.infrastructure.octofront.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.octopus.loginmessage.CommercialAzureServiceBusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import java.time.ZonedDateTime;
import java.util.List;
import javax.inject.Inject;
import org.apache.maven.shared.utils.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * This test ensures the message generated for the service bus has the required properties as
 * documented in https://github.com/OctopusDeploy/Architecture/blob/main/OctopusHQ/Strategy.md.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(CommercialAzureServiceBusTestProfile.class)
public class CommercialServiceBusImplTest {

  private static List<String> VALID_PROPERTIES = List.of(
      "context",
      "source",
      "type",
      "occurredTimeUtc",
      "activityId",
      "specVersion");

  @Inject
  CommercialServiceBusImpl commercialServiceBus;

  @Test
  public void testGenerateMessage() {
    final ServiceBusMessage message = commercialServiceBus.generateMessage("trace", "body");

    // All properties should be expected
    message.getApplicationProperties().keySet().forEach(k -> assertTrue(
        VALID_PROPERTIES.contains(k)));
    // All properties should be set
    message.getApplicationProperties().keySet().forEach(k -> assertTrue(
        StringUtils.isNotBlank(message.getApplicationProperties().get(k).toString())));
    // This field should be a ISO-8601 datetime
    assertDoesNotThrow(() -> ZonedDateTime.parse(message.getApplicationProperties().get("occurredTimeUtc").toString()));
    // These fields have known values
    assertEquals("body", message.getBody().toString());
    assertEquals("trace", message.getApplicationProperties().get("activityId").toString());
    assertEquals("marketing", message.getApplicationProperties().get("context").toString());
    assertEquals("1", message.getApplicationProperties().get("specVersion").toString());
    assertEquals("GithubUserLoggedInForFreeToolsEventV1".toLowerCase(), message.getApplicationProperties().get("type").toString());
  }

  @Test
  public void testInvalidArgs() {
    assertThrows(NullPointerException.class, () -> commercialServiceBus.sendUserDetails("", null));
    assertThrows(IllegalArgumentException.class,
        () -> commercialServiceBus.sendUserDetails("", ""));
  }

  @Test
  public void testSend() {
    assertDoesNotThrow(() -> commercialServiceBus.sendUserDetails("", "body"));
    assertDoesNotThrow(() -> commercialServiceBus.sendUserDetails(null, "body"));
    assertDoesNotThrow(() -> commercialServiceBus.sendUserDetails("traceid", "body"));
  }
}
