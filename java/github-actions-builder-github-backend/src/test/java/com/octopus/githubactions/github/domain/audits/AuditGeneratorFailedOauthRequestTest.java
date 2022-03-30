package com.octopus.githubactions.github.domain.audits;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.octopus.githubactions.github.domain.TestingProfile;
import com.octopus.githubactions.github.domain.entities.Audit;
import com.octopus.githubactions.github.infrastructure.client.AuditClient;
import com.octopus.githubactions.github.infrastructure.client.CognitoClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Verifies the AuditGenerator fails to send a request when the the call to the auth server fails.
 */
@QuarkusTest
@TestProfile(TestingProfile.class)
public class AuditGeneratorFailedOauthRequestTest {

  private static final String XRAY = "xray";
  private static final String ROUTING = "routing";
  private static final String PARTITION = "partition";
  private static final String AUTH = "auth";

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
    // Simulate a failed request to get oauth creds.
    Mockito.when(cognitoClient.getToken(any(), any(), any(), any())).thenThrow(new RuntimeException());

    doAnswer(invocation -> {
      // This should never be called if we don't get OAuth creds.
      Assertions.fail();
      return null;
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
  }
}
