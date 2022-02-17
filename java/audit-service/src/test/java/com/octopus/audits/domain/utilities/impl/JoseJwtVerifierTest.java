package com.octopus.audits.domain.utilities.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbusds.jose.JOSEException;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.text.ParseException;
import java.util.Base64;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class JoseJwtVerifierTest {

  private static final String EXPIRED_M2M_JWT = "eyJraWQiOiJUemtBZlNIakQxWEl0VFYzWFwvWEtlaTZTVHRVSHcwemdqYzhOQjVnM1J4QT0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI3MTBvY3RhdjJxZHU0ZmpkNXYzdTNxYjFwOCIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiYXVkaXQuY29udGVudC10ZWFtXC9hZG1pbiIsImF1dGhfdGltZSI6MTY0NDUyMzE0MCwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLXdlc3QtMS5hbWF6b25hd3MuY29tXC91cy13ZXN0LTFfVmtBZm56cVpGIiwiZXhwIjoxNjQ0NTI2NzQwLCJpYXQiOjE2NDQ1MjMxNDAsInZlcnNpb24iOjIsImp0aSI6ImVlOTUzMzFhLTI1YTAtNGM5Mi04YjE5LTlmNzE2OGJjZGQ4NCIsImNsaWVudF9pZCI6IjcxMG9jdGF2MnFkdTRmamQ1djN1M3FiMXA4In0.EhKwAissnimAJp_Py2n-s6sWqodPt9O5wW3MZrkrA34mfzqRIVwDOAytosrOjyQMryhKf2_YHMPuCSD0QaTb9yhBtp5Djz0txkyPQFVbGc6DURpBB-rFvr_cbr22amSMGi1v6RcfXNcbEgZdUFEbjyEwubuTUAo_7mW8Dxr3Zohr2CCTIxMtxdjrtGvcdQPT0_E25Bw8bJxMmOTQw662hpb9XQS9xnobuR6qyBfIA7oHaJV3RTFz0lU1s1i0qtSCqtqxpqEhA8BkalvKIdzh7_cuycXnY3VbF4BOAOZxwvQ1rrscqjXQdr6cqkZetZDSE2g5_5LB1Ki60IA_5WCQBg";
  private static final String EXPIRED_USER_JWT = "eyJraWQiOiJUemtBZlNIakQxWEl0VFYzWFwvWEtlaTZTVHRVSHcwemdqYzhOQjVnM1J4QT0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxM2FiYzc2Yi0xMWZmLTQ2YWUtODQyMS1hMzk4ZGM0Y2YyODYiLCJjb2duaXRvOmdyb3VwcyI6WyJEZXZlbG9wZXJzIiwidXMtd2VzdC0xX1ZrQWZuenFaRl9Hb29nbGUiXSwidG9rZW5fdXNlIjoiYWNjZXNzIiwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCIsImF1dGhfdGltZSI6MTY0NTA3NDQwNywiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLXdlc3QtMS5hbWF6b25hd3MuY29tXC91cy13ZXN0LTFfVmtBZm56cVpGIiwiZXhwIjoxNjQ1MDc4MDA3LCJpYXQiOjE2NDUwNzQ0MDcsInZlcnNpb24iOjIsImp0aSI6ImI5NGJmNmU5LTU3MjYtNDQwMy05NGY3LTM2ZjVmYmNlNmI3ZSIsImNsaWVudF9pZCI6IjRyMGF0ZmY3b3ZxYmhycGUxNzczazM3dms5IiwidXNlcm5hbWUiOiJnb29nbGVfMTA5MDAzMTYxMzQ4MjI5NTg3NzUxIn0.aoBrMPbOYZ42JLGPIEj_N5wbkJG14EJjIHzXyuVOVw5IN7LU0WwXE7YV_0fJaeZNzNu7KF_FRFgOfOXIOZKB-RsKw9lpQETHF1od83PnLowoyzDwu5W44aH-LwVnVnOuhKRmomvmOAEM6Rdc-EC6HoZYOXPRuvDe6a1ILPIhpqhpJ6krKXPVXlt5h6M2own3RXk_dkdxwDfleqS7QIW2feYkIdEyGPptF_4c1wztAYpAmpeNQoTFh-cgOo9rfT9dZ089delsJF-uikgy-5Dh83o2VTbGoMRqCUVOS6-i9lnxBN5FxKLSmd6y5YPgYJwmys9v2lrJZNWXbQ5A2RuPhg";
  private static final String M2M_CLIENT_ID = "710octav2qdu4fjd5v3u3qb1p8";
  private static final String USER_CLIENT_ID = "4r0atff7ovqbhrpe1773k37vk9";
  private static final String JWK = "{\"keys\":[{\"alg\":\"RS256\",\"e\":\"AQAB\",\"kid\":\"3e6hWRotCd1nqDE5jwzaHW5AELzm2TPl5ADJPM2OOYQ=\",\"kty\":\"RSA\",\"n\":\"0cripNZ9mj_4zuUGkESqZAw2IU2FvmTGdZHk2IxxoAdzz5ESpkqA5UbSoqVm7avAmNf-8jNWaQfjtCPphHVrsjAKqKgUc_haL4kkO5PS_78wCXnq_Yr7d4OyQexD1hgoiI-Ic89Q8R3px1OE1RVVwA815Fvp5OqfVQJc-rpoPaI1rJM02XrpOlKd-PN_bAqYXbpNtwU-kB85ieeoLt3N4EI1RpBB8ZfzWp0YeLYN5jBTRGalmcplQGBTlXJPtMzjLT76cjEgcRVRNZSMWApma-XrWo7UQDdzGZb56_ba2Nx-cSytLwNVyvXeJIcMkKrnu3Gs2jg_jQ5GQbjf3ohygQ\",\"use\":\"sig\"},{\"alg\":\"RS256\",\"e\":\"AQAB\",\"kid\":\"TzkAfSHjD1XItTV3X/XKei6STtUHw0zgjc8NB5g3RxA=\",\"kty\":\"RSA\",\"n\":\"1KPkC5PA9n9g8uYFOaFdR7QgndxSBMYUxQApjrfh-7TCeKBTcW55uHz8FBa2YD3Kl7ei86Zyx7xdcPndouutZIfqMD5Ff6xbXeZ2Q-OCj-3RgnWU-Ie5DI--VjjslG-NlRZYDLpIDyDqho_bRIQ_tuemxDJc_-jJVFjGHUKKsLC6BYifGylqgyILJWjFYadWOoqr4EWE-pvng8bUnT6a1nZTjNtfGUdabp_pHxvi6LWVAeOKkrswZ0vMlKxThh46v0ZsQwqyjjxeSnRBqh_z9wtSleUXfRAXoLZdBpmfxBWoCk7CiGdeaThVx_Amw6qGYK1uX60vtNjRJRDMqy2d3Q\",\"use\":\"sig\"}]}";
  private static final JoseJwtVerifier JOSE_JWT_VERIFIER = new JoseJwtVerifier();

  @Test
  public void verifyClaimsExtraction() {
    assertTrue(JOSE_JWT_VERIFIER.jwtContainsScope(EXPIRED_M2M_JWT, "audit.content-team/admin",
        M2M_CLIENT_ID, true));
  }

  @Test
  public void verifyGroupExtraction() {
    assertTrue(JOSE_JWT_VERIFIER.jwtContainsCognitoGroup(EXPIRED_USER_JWT, "Developers", true));
  }

  @Test()
  public void verifyTokenExpired() throws ParseException, IOException, JOSEException {
    final String jwkBase64 = Base64.getEncoder().encodeToString(JWK.getBytes());
    assertFalse(JOSE_JWT_VERIFIER.jwtIsValid(EXPIRED_M2M_JWT, jwkBase64));
  }

  @Test()
  public void verifyTokenNotValid() {
    assertThrows(ParseException.class, () -> {
      final String jwkBase64 = Base64.getEncoder().encodeToString(JWK.getBytes());
      JOSE_JWT_VERIFIER.jwtIsValid("blah", jwkBase64);
    });
  }
}
