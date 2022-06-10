package com.octopus.githubrepo.domain.audit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.cognito.CognitoAccessTokenGenerator;
import com.octopus.githubrepo.domain.entities.Audit;
import com.octopus.githubrepo.infrastructure.clients.AuditClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.vavr.control.Try;
import java.io.IOException;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@TestProfile(TestingProfile.class)
public class AuditGeneratorFailedAuthTest {
  @InjectMock
  CognitoAccessTokenGenerator cognitoAccessTokenGenerator;

  @InjectMock
  @RestClient
  AuditClient auditClient;

  @Inject
  AuditGenerator auditGenerator;

  @BeforeEach
  public void setup() throws IOException {
    Mockito.when(cognitoAccessTokenGenerator.getAccessToken()).thenReturn(Try.failure(new Exception()));
    Mockito.when(auditClient.createAudit(any(), any(), any(), any(), any(), any(), any())).thenReturn("audit");
  }

  @Test
  public void createAuditEventButFailAccessToken() {
    final Audit audit = Audit.builder()
        .action("action")
        .subject("subject")
        .object("object")
        .build();

    // We should not throw if the access token can not be retrieved
    assertDoesNotThrow(() -> auditGenerator.createAuditEvent(
        audit,
        "xray",
        "routing",
        "partition",
        "auth"));

    // But we should not make the call to create an audit
    verify(auditClient, times(0)).createAudit(any(), any(), any(), any(), any(), any(), any());
    verify(cognitoAccessTokenGenerator, times(1)).getAccessToken();
  }
}
