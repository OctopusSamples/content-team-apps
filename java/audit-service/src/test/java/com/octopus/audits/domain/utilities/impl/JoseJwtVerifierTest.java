package com.octopus.audits.domain.utilities.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import com.nimbusds.jose.JOSEException;
import com.octopus.audits.domain.features.CognitoJwkBase64Feature;
import com.octopus.audits.domain.features.DisableSecurityFeature;
import com.octopus.audits.domain.utilities.JwtUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
public class JoseJwtVerifierTest {

  private static final String EXPIRED_M2M_JWT = "eyJraWQiOiJUemtBZlNIakQxWEl0VFYzWFwvWEtlaTZTVHRVSHcwemdqYzhOQjVnM1J4QT0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI3MTBvY3RhdjJxZHU0ZmpkNXYzdTNxYjFwOCIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiYXVkaXQuY29udGVudC10ZWFtXC9hZG1pbiIsImF1dGhfdGltZSI6MTY0NDUyMzE0MCwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLXdlc3QtMS5hbWF6b25hd3MuY29tXC91cy13ZXN0LTFfVmtBZm56cVpGIiwiZXhwIjoxNjQ0NTI2NzQwLCJpYXQiOjE2NDQ1MjMxNDAsInZlcnNpb24iOjIsImp0aSI6ImVlOTUzMzFhLTI1YTAtNGM5Mi04YjE5LTlmNzE2OGJjZGQ4NCIsImNsaWVudF9pZCI6IjcxMG9jdGF2MnFkdTRmamQ1djN1M3FiMXA4In0.EhKwAissnimAJp_Py2n-s6sWqodPt9O5wW3MZrkrA34mfzqRIVwDOAytosrOjyQMryhKf2_YHMPuCSD0QaTb9yhBtp5Djz0txkyPQFVbGc6DURpBB-rFvr_cbr22amSMGi1v6RcfXNcbEgZdUFEbjyEwubuTUAo_7mW8Dxr3Zohr2CCTIxMtxdjrtGvcdQPT0_E25Bw8bJxMmOTQw662hpb9XQS9xnobuR6qyBfIA7oHaJV3RTFz0lU1s1i0qtSCqtqxpqEhA8BkalvKIdzh7_cuycXnY3VbF4BOAOZxwvQ1rrscqjXQdr6cqkZetZDSE2g5_5LB1Ki60IA_5WCQBg";
  private static final String EXPIRED_USER_JWT = "eyJraWQiOiJUemtBZlNIakQxWEl0VFYzWFwvWEtlaTZTVHRVSHcwemdqYzhOQjVnM1J4QT0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxM2FiYzc2Yi0xMWZmLTQ2YWUtODQyMS1hMzk4ZGM0Y2YyODYiLCJjb2duaXRvOmdyb3VwcyI6WyJEZXZlbG9wZXJzIiwidXMtd2VzdC0xX1ZrQWZuenFaRl9Hb29nbGUiXSwidG9rZW5fdXNlIjoiYWNjZXNzIiwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCIsImF1dGhfdGltZSI6MTY0NTA3NDQwNywiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLXdlc3QtMS5hbWF6b25hd3MuY29tXC91cy13ZXN0LTFfVmtBZm56cVpGIiwiZXhwIjoxNjQ1MDc4MDA3LCJpYXQiOjE2NDUwNzQ0MDcsInZlcnNpb24iOjIsImp0aSI6ImI5NGJmNmU5LTU3MjYtNDQwMy05NGY3LTM2ZjVmYmNlNmI3ZSIsImNsaWVudF9pZCI6IjRyMGF0ZmY3b3ZxYmhycGUxNzczazM3dms5IiwidXNlcm5hbWUiOiJnb29nbGVfMTA5MDAzMTYxMzQ4MjI5NTg3NzUxIn0.aoBrMPbOYZ42JLGPIEj_N5wbkJG14EJjIHzXyuVOVw5IN7LU0WwXE7YV_0fJaeZNzNu7KF_FRFgOfOXIOZKB-RsKw9lpQETHF1od83PnLowoyzDwu5W44aH-LwVnVnOuhKRmomvmOAEM6Rdc-EC6HoZYOXPRuvDe6a1ILPIhpqhpJ6krKXPVXlt5h6M2own3RXk_dkdxwDfleqS7QIW2feYkIdEyGPptF_4c1wztAYpAmpeNQoTFh-cgOo9rfT9dZ089delsJF-uikgy-5Dh83o2VTbGoMRqCUVOS6-i9lnxBN5FxKLSmd6y5YPgYJwmys9v2lrJZNWXbQ5A2RuPhg";
  private static final String JWK = "eyJrZXlzIjpbeyJhbGciOiJSUzI1NiIsImUiOiJBUUFCIiwia2lkIjoiM2U2aFdSb3RDZDFucURFNWp3emFIVzVBRUx6bTJUUGw1QURKUE0yT09ZUT0iLCJrdHkiOiJSU0EiLCJuIjoiMGNyaXBOWjltal80enVVR2tFU3FaQXcySVUyRnZtVEdkWkhrMkl4eG9BZHp6NUVTcGtxQTVVYlNvcVZtN2F2QW1OZi04ak5XYVFmanRDUHBoSFZyc2pBS3FLZ1VjX2hhTDRra081UFNfNzh3Q1hucV9ZcjdkNE95UWV4RDFoZ29pSS1JYzg5UThSM3B4MU9FMVJWVndBODE1RnZwNU9xZlZRSmMtcnBvUGFJMXJKTTAyWHJwT2xLZC1QTl9iQXFZWGJwTnR3VS1rQjg1aWVlb0x0M040RUkxUnBCQjhaZnpXcDBZZUxZTjVqQlRSR2FsbWNwbFFHQlRsWEpQdE16akxUNzZjakVnY1JWUk5aU01XQXBtYS1YcldvN1VRRGR6R1piNTZfYmEyTngtY1N5dEx3TlZ5dlhlSkljTWtLcm51M0dzMmpnX2pRNUdRYmpmM29oeWdRIiwidXNlIjoic2lnIn0seyJhbGciOiJSUzI1NiIsImUiOiJBUUFCIiwia2lkIjoiVHprQWZTSGpEMVhJdFRWM1gvWEtlaTZTVHRVSHcwemdqYzhOQjVnM1J4QT0iLCJrdHkiOiJSU0EiLCJuIjoiMUtQa0M1UEE5bjlnOHVZRk9hRmRSN1FnbmR4U0JNWVV4UUFwanJmaC03VENlS0JUY1c1NXVIejhGQmEyWUQzS2w3ZWk4Nlp5eDd4ZGNQbmRvdXV0WklmcU1ENUZmNnhiWGVaMlEtT0NqLTNSZ25XVS1JZTVESS0tVmpqc2xHLU5sUlpZRExwSUR5RHFob19iUklRX3R1ZW14REpjXy1qSlZGakdIVUtLc0xDNkJZaWZHeWxxZ3lJTEpXakZZYWRXT29xcjRFV0UtcHZuZzhiVW5UNmExblpUak50ZkdVZGFicF9wSHh2aTZMV1ZBZU9La3Jzd1owdk1sS3hUaGg0NnYwWnNRd3F5amp4ZVNuUkJxaF96OXd0U2xlVVhmUkFYb0xaZEJwbWZ4QldvQ2s3Q2lHZGVhVGhWeF9BbXc2cUdZSzF1WDYwdnROalJKUkRNcXkyZDNRIiwidXNlIjoic2lnIn1dfQ==";
  private static final String M2M_CLIENT_ID = "710octav2qdu4fjd5v3u3qb1p8";
  private static final String USER_CLIENT_ID = "4r0atff7ovqbhrpe1773k37vk9";
  private static final String SIMPLE_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

  @Inject
  JoseJwtVerifier joseJwtVerifier;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @InjectMock
  CognitoJwkBase64Feature cognitoJwkBase64Feature;

  @InjectMock
  JwtValidator jwtValidator;

  @BeforeEach
  public void setup()
      throws SQLException, LiquibaseException, ParseException, IOException, JOSEException {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(true);
    Mockito.when(cognitoJwkBase64Feature.getCognitoJwk()).thenReturn(Optional.of(JWK));
    Mockito.when(jwtValidator.jwtIsValid(any(), any())).thenReturn(true);
  }

  @Test
  public void verifyClaimsExtraction() {
    assertTrue(joseJwtVerifier.jwtContainsScope(EXPIRED_M2M_JWT, "audit.content-team/admin",
        M2M_CLIENT_ID));
  }

  @Test
  public void verifyBadJwtClaimsExtraction() {
    assertFalse(joseJwtVerifier.jwtContainsScope("blah", "audit.content-team/admin",
        M2M_CLIENT_ID));
  }

  @Test
  public void verifyBadClientClaimsExtraction() {
    assertFalse(joseJwtVerifier.jwtContainsScope(EXPIRED_M2M_JWT, "audit.content-team/admin",
        "Unknown"));
  }

  @Test
  public void verifyClientIdExtraction() throws ParseException {
    assertEquals(M2M_CLIENT_ID, joseJwtVerifier.extractClientId(EXPIRED_M2M_JWT).get());
  }

  @Test
  public void verifyScopeExtraction() throws ParseException {
    assertFalse(joseJwtVerifier.extractScope(EXPIRED_M2M_JWT).isEmpty());
  }

  @Test
  public void verifyScopeContains() throws ParseException {
    final List<String> scopes = joseJwtVerifier.extractScope(EXPIRED_M2M_JWT);
    assertTrue(joseJwtVerifier.extractScope(EXPIRED_M2M_JWT).contains("audit.content-team/admin"));
  }

  @Test
  public void verifyMissingClientIdExtraction() throws ParseException {
    assertTrue(joseJwtVerifier.extractClientId(SIMPLE_JWT).isEmpty());
  }

  @Test
  public void verifyMissingClaimExtraction() throws ParseException {
    assertTrue(joseJwtVerifier.extractScope(SIMPLE_JWT).isEmpty());
  }

  @Test
  public void verifyBadClaimClaimsExtraction() {
    assertFalse(joseJwtVerifier.jwtContainsScope(EXPIRED_M2M_JWT, "unknown",
        M2M_CLIENT_ID));
  }

  @Test
  public void verifyGroupExtraction() {
    assertTrue(joseJwtVerifier.jwtContainsCognitoGroup(EXPIRED_USER_JWT, "Developers"));
  }

  @Test
  public void verifyBadJwtGroupExtraction() {
    assertFalse(joseJwtVerifier.jwtContainsCognitoGroup("blah", "Developers"));
  }

  @Test
  public void verifyBadGroupExtraction() {
    assertFalse(joseJwtVerifier.jwtContainsCognitoGroup(EXPIRED_USER_JWT, "unknown"));
  }

  @Test
  public void verifyMissingGroupExtraction() {
    assertFalse(joseJwtVerifier.jwtContainsCognitoGroup(SIMPLE_JWT, "unknown"));
  }
}
