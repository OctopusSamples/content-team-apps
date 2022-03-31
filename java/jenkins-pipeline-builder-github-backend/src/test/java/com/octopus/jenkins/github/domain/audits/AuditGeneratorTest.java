package com.octopus.jenkins.github.domain.audits;

import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octopus.jenkins.github.domain.TestingProfile;
import com.octopus.jenkins.github.domain.entities.Audit;
import com.octopus.jenkins.github.infrastructure.client.AuditClient;
import com.octopus.jenkins.github.infrastructure.client.CognitoClient;
import com.octopus.oauth.Oauth;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies the AuditGenerator passes a request through to the rest client OK.
 */
@QuarkusTest
@TestProfile(TestingProfile.class)
public class AuditGeneratorTest {

  private static final String XRAY = "xray";
  private static final String ROUTING = "routing";
  private static final String PARTITION = "partition";
  private static final String AUTH = "auth";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private int externalApiCalls = 0;

  @Inject
  AuditGenerator auditGenerator;

  @InjectMock
  @RestClient
  AuditClient auditClient;

  @InjectMock
  @RestClient
  CognitoClient cognitoClient;

  @BeforeEach
  public void setup() {
    doAnswer(invocation -> {
      ++externalApiCalls;

      // The access key should be cached, and we only make this call once
      assertTrue(externalApiCalls <= 1);

      return Oauth.builder()
          .accessToken("accessToken")
          .expiresIn(Integer.MAX_VALUE)
          .build();
    })
        .when(cognitoClient)
        .getToken(any(), any(), any(), any());

    doAnswer(invocation -> {
      final String audit = invocation.getArgument(0);
      final String xray = invocation.getArgument(1);
      final String routing = invocation.getArgument(2);
      final String partition = invocation.getArgument(3);
      final String auth = invocation.getArgument(4);

      assertDoesNotThrow(() -> OBJECT_MAPPER.readValue(audit, Map.class));

      assertEquals(XRAY, xray);
      assertEquals(ROUTING, routing);
      assertEquals(PARTITION, partition);
      assertEquals(AUTH, auth);
      return "Success!";
    })
        .when(auditClient)
        .createAudit(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  public void testAuditGenerator() {
    Assertions.assertDoesNotThrow(() -> auditGenerator.createAuditEvent(
        Audit
            .builder()
            .action("action")
            .object("object")
            .subject("subject")
            .build(),
        XRAY,
        ROUTING,
        PARTITION,
        AUTH));

    // Call again to test the access key is cached.
    Assertions.assertDoesNotThrow(() -> auditGenerator.createAuditEvent(
        Audit
            .builder()
            .action("action")
            .object("object")
            .subject("subject")
            .build(),
        XRAY,
        ROUTING,
        PARTITION,
        AUTH));

    assertEquals(1, externalApiCalls, "The access token should have been requested once.");
  }

  @Test
  public void testAuditGeneratorNullArgs() {
    Assertions.assertThrows(NullPointerException.class, () -> auditGenerator.createAuditEvent(
        null,
        XRAY,
        ROUTING,
        PARTITION,
        AUTH));

    Assertions.assertThrows(NullPointerException.class, () -> auditGenerator.createAuditEvent(
        Audit
            .builder()
            .action("action")
            .object("object")
            .subject("subject")
            .build(),
        XRAY,
        null,
        PARTITION,
        AUTH));

    Assertions.assertThrows(NullPointerException.class, () -> auditGenerator.createAuditEvent(
        Audit
            .builder()
            .action("action")
            .object("object")
            .subject("subject")
            .build(),
        XRAY,
        ROUTING,
        null,
        AUTH));

    Assertions.assertThrows(NullPointerException.class, () -> auditGenerator.createAuditEvent(
        Audit
            .builder()
            .action("action")
            .object("object")
            .subject("subject")
            .build(),
        XRAY,
        ROUTING,
        PARTITION,
        null));
  }
}
