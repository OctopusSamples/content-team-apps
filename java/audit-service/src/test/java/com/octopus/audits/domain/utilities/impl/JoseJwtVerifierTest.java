package com.octopus.audits.domain.utilities.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import com.nimbusds.jose.JOSEException;
import com.octopus.audits.domain.utilities.DisableSecurityFeature;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.io.IOException;
import java.text.ParseException;
import java.util.Base64;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class JoseJwtVerifierTest {

  private static final String EXPIRED_M2M_JWT = "eyJraWQiOiJUemtBZlNIakQxWEl0VFYzWFwvWEtlaTZTVHRVSHcwemdqYzhOQjVnM1J4QT0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI3MTBvY3RhdjJxZHU0ZmpkNXYzdTNxYjFwOCIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiYXVkaXQuY29udGVudC10ZWFtXC9hZG1pbiIsImF1dGhfdGltZSI6MTY0NDUyMzE0MCwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLXdlc3QtMS5hbWF6b25hd3MuY29tXC91cy13ZXN0LTFfVmtBZm56cVpGIiwiZXhwIjoxNjQ0NTI2NzQwLCJpYXQiOjE2NDQ1MjMxNDAsInZlcnNpb24iOjIsImp0aSI6ImVlOTUzMzFhLTI1YTAtNGM5Mi04YjE5LTlmNzE2OGJjZGQ4NCIsImNsaWVudF9pZCI6IjcxMG9jdGF2MnFkdTRmamQ1djN1M3FiMXA4In0.EhKwAissnimAJp_Py2n-s6sWqodPt9O5wW3MZrkrA34mfzqRIVwDOAytosrOjyQMryhKf2_YHMPuCSD0QaTb9yhBtp5Djz0txkyPQFVbGc6DURpBB-rFvr_cbr22amSMGi1v6RcfXNcbEgZdUFEbjyEwubuTUAo_7mW8Dxr3Zohr2CCTIxMtxdjrtGvcdQPT0_E25Bw8bJxMmOTQw662hpb9XQS9xnobuR6qyBfIA7oHaJV3RTFz0lU1s1i0qtSCqtqxpqEhA8BkalvKIdzh7_cuycXnY3VbF4BOAOZxwvQ1rrscqjXQdr6cqkZetZDSE2g5_5LB1Ki60IA_5WCQBg";
  private static final String EXPIRED_USER_JWT = "eyJraWQiOiJUemtBZlNIakQxWEl0VFYzWFwvWEtlaTZTVHRVSHcwemdqYzhOQjVnM1J4QT0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxM2FiYzc2Yi0xMWZmLTQ2YWUtODQyMS1hMzk4ZGM0Y2YyODYiLCJjb2duaXRvOmdyb3VwcyI6WyJEZXZlbG9wZXJzIiwidXMtd2VzdC0xX1ZrQWZuenFaRl9Hb29nbGUiXSwidG9rZW5fdXNlIjoiYWNjZXNzIiwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCIsImF1dGhfdGltZSI6MTY0NTA3NDQwNywiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLXdlc3QtMS5hbWF6b25hd3MuY29tXC91cy13ZXN0LTFfVmtBZm56cVpGIiwiZXhwIjoxNjQ1MDc4MDA3LCJpYXQiOjE2NDUwNzQ0MDcsInZlcnNpb24iOjIsImp0aSI6ImI5NGJmNmU5LTU3MjYtNDQwMy05NGY3LTM2ZjVmYmNlNmI3ZSIsImNsaWVudF9pZCI6IjRyMGF0ZmY3b3ZxYmhycGUxNzczazM3dms5IiwidXNlcm5hbWUiOiJnb29nbGVfMTA5MDAzMTYxMzQ4MjI5NTg3NzUxIn0.aoBrMPbOYZ42JLGPIEj_N5wbkJG14EJjIHzXyuVOVw5IN7LU0WwXE7YV_0fJaeZNzNu7KF_FRFgOfOXIOZKB-RsKw9lpQETHF1od83PnLowoyzDwu5W44aH-LwVnVnOuhKRmomvmOAEM6Rdc-EC6HoZYOXPRuvDe6a1ILPIhpqhpJ6krKXPVXlt5h6M2own3RXk_dkdxwDfleqS7QIW2feYkIdEyGPptF_4c1wztAYpAmpeNQoTFh-cgOo9rfT9dZ089delsJF-uikgy-5Dh83o2VTbGoMRqCUVOS6-i9lnxBN5FxKLSmd6y5YPgYJwmys9v2lrJZNWXbQ5A2RuPhg";
  private static final String M2M_CLIENT_ID = "710octav2qdu4fjd5v3u3qb1p8";
  private static final String USER_CLIENT_ID = "4r0atff7ovqbhrpe1773k37vk9";
  private static final String SIMPLE_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
  private static final JoseJwtVerifierAlwaysValid JOSE_JWT_VERIFIER_ALWAYS_VALID = new JoseJwtVerifierAlwaysValid();

  @Test
  public void verifyClaimsExtraction() {
    assertTrue(JOSE_JWT_VERIFIER_ALWAYS_VALID.jwtContainsScope(EXPIRED_M2M_JWT, "audit.content-team/admin",
        M2M_CLIENT_ID));
  }

  @Test
  public void verifyBadJwtClaimsExtraction() {
    assertFalse(JOSE_JWT_VERIFIER_ALWAYS_VALID.jwtContainsScope("blah", "audit.content-team/admin",
        M2M_CLIENT_ID));
  }

  @Test
  public void verifyBadClientClaimsExtraction() {
    assertFalse(JOSE_JWT_VERIFIER_ALWAYS_VALID.jwtContainsScope(EXPIRED_M2M_JWT, "audit.content-team/admin",
        "Unknown"));
  }

  @Test
  public void verifyClientIdExtraction() throws ParseException {
    assertEquals(M2M_CLIENT_ID, JOSE_JWT_VERIFIER_ALWAYS_VALID.extractClientId(EXPIRED_M2M_JWT).get());
  }

  @Test
  public void verifyMissingClientIdExtraction() throws ParseException {
    assertTrue(JOSE_JWT_VERIFIER_ALWAYS_VALID.extractClientId(SIMPLE_JWT).isEmpty());
  }

  @Test
  public void verifyMissingClaimExtraction() throws ParseException {
    assertTrue(JOSE_JWT_VERIFIER_ALWAYS_VALID.extractScope(SIMPLE_JWT).isEmpty());
  }

  @Test
  public void verifyBadClaimClaimsExtraction() {
    assertFalse(JOSE_JWT_VERIFIER_ALWAYS_VALID.jwtContainsScope(EXPIRED_M2M_JWT, "unknown",
        M2M_CLIENT_ID));
  }

  @Test
  public void verifyGroupExtraction() {
    assertTrue(JOSE_JWT_VERIFIER_ALWAYS_VALID.jwtContainsCognitoGroup(EXPIRED_USER_JWT, "Developers"));
  }

  @Test
  public void verifyBadJwtGroupExtraction() {
    assertFalse(JOSE_JWT_VERIFIER_ALWAYS_VALID.jwtContainsCognitoGroup("blah", "Developers"));
  }

  @Test
  public void verifyBadGroupExtraction() {
    assertFalse(JOSE_JWT_VERIFIER_ALWAYS_VALID.jwtContainsCognitoGroup(EXPIRED_USER_JWT, "unknown"));
  }

  @Test
  public void verifyMissingGroupExtraction() {
    assertFalse(JOSE_JWT_VERIFIER_ALWAYS_VALID.jwtContainsCognitoGroup(SIMPLE_JWT, "unknown"));
  }
}
