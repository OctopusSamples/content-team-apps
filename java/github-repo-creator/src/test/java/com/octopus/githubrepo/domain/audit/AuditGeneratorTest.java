package com.octopus.githubrepo.domain.audit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.cognito.CognitoAccessTokenGenerator;
import com.octopus.githubrepo.domain.entities.Audit;
import com.octopus.githubrepo.domain.framework.producers.JsonApiConverter;
import com.octopus.githubrepo.infrastructure.clients.AuditClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.vavr.control.Try;
import java.io.IOException;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(Lifecycle.PER_METHOD)
@TestProfile(TestingProfile.class)
public class AuditGeneratorTest {
  @InjectMock
  CognitoAccessTokenGenerator cognitoAccessTokenGenerator;

  @InjectMock
  @RestClient
  AuditClient auditClient;

  @Inject
  AuditGenerator auditGenerator;

  @Inject
  JsonApiConverter jsonApiConverter;

  @BeforeEach
  public void setup() throws IOException {
    Mockito.when(cognitoAccessTokenGenerator.getAccessToken()).thenReturn(Try.of(() -> "accesstoken"));
    Mockito.when(auditClient.createAudit(any(), any(), any(), any(), any(), any(), any())).thenReturn("audit");
  }

  @Test
  public void createAuditEvent() throws DocumentSerializationException {
    final Audit audit = Audit.builder()
        .action("action")
        .subject("subject")
        .object("object")
        .build();

    final String auditString = new String(jsonApiConverter.buildResourceConverter().writeDocument(
        new JSONAPIDocument<>(audit)));

    assertDoesNotThrow(() -> auditGenerator.createAuditEvent(
        audit,
        "xray",
        "routing",
        "partition",
        "auth"));

    verify(auditClient).createAudit(
        eq(auditString),
        eq("xray"),
        eq("routing"),
        eq("partition"),
        eq("auth"),
        any(),
        any());

    // Auth is disabled, so we should not try to generate a token
    verify(cognitoAccessTokenGenerator, times(0)).getAccessToken();
  }

  @Test
  public void testNullArgs() {
    final Audit audit = Audit.builder()
        .action("action")
        .subject("subject")
        .object("object")
        .build();

    assertThrows(NullPointerException.class, () -> auditGenerator.createAuditEvent(
        null,
        "xray",
        "routing",
        "partition",
        "auth"));

    assertThrows(NullPointerException.class, () -> auditGenerator.createAuditEvent(
        audit,
        "xray",
        null,
        "partition",
        "auth"));

    assertThrows(NullPointerException.class, () -> auditGenerator.createAuditEvent(
        audit,
        "xray",
        "routing",
        null,
        "auth"));

    assertThrows(NullPointerException.class, () -> auditGenerator.createAuditEvent(
        audit,
        "xray",
        "routing",
        "partition",
        null));
  }
}
