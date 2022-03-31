package com.octopus.jenkins.github.domain.audits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.octopus.jenkins.github.domain.TestingProfile;
import com.octopus.jenkins.github.domain.entities.Audit;
import com.octopus.jenkins.github.infrastructure.client.AuditClient;
import com.octopus.jenkins.github.infrastructure.client.CognitoClient;
import com.octopus.oauth.Oauth;
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
 * Verifies the AuditGenerator refreshes the access token if it is expired.
 */
@QuarkusTest
@TestProfile(TestingProfile.class)
public class AuditGeneratorRefreshAccessTokenTest {

  private static final String XRAY = "xray";
  private static final String ROUTING = "routing";
  private static final String PARTITION = "partition";
  private static final String AUTH = "auth";

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

      return Oauth.builder()
          .accessToken("accessToken")
          // The min value here assures the token will be considered expired next run
          .expiresIn(Integer.MIN_VALUE)
          .build();
    })
        .when(cognitoClient)
        .getToken(any(), any(), any(), any());

    Mockito
        .when(auditClient.createAudit(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn("Success");
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

    // Call again to test the access key is refreshed.
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

    assertEquals(2, externalApiCalls, "The access token should have been requested twice.");
  }


}
