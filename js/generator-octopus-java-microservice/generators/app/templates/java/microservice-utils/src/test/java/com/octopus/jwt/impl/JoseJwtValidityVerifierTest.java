package com.octopus.jwt.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nimbusds.jose.JOSEException;
import com.octopus.jwt.JwtValidator;
import java.io.IOException;
import java.text.ParseException;
import java.util.Base64;
import org.junit.jupiter.api.Test;

public class JoseJwtValidityVerifierTest {

  private static final String EXPIRED_M2M_JWT = "eyJraWQiOiJUemtBZlNIakQxWEl0VFYzWFwvWEtlaTZTVHRVSHcwemdqYzhOQjVnM1J4QT0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI3MTBvY3RhdjJxZHU0ZmpkNXYzdTNxYjFwOCIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiYXVkaXQuY29udGVudC10ZWFtXC9hZG1pbiIsImF1dGhfdGltZSI6MTY0NDUyMzE0MCwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLXdlc3QtMS5hbWF6b25hd3MuY29tXC91cy13ZXN0LTFfVmtBZm56cVpGIiwiZXhwIjoxNjQ0NTI2NzQwLCJpYXQiOjE2NDQ1MjMxNDAsInZlcnNpb24iOjIsImp0aSI6ImVlOTUzMzFhLTI1YTAtNGM5Mi04YjE5LTlmNzE2OGJjZGQ4NCIsImNsaWVudF9pZCI6IjcxMG9jdGF2MnFkdTRmamQ1djN1M3FiMXA4In0.EhKwAissnimAJp_Py2n-s6sWqodPt9O5wW3MZrkrA34mfzqRIVwDOAytosrOjyQMryhKf2_YHMPuCSD0QaTb9yhBtp5Djz0txkyPQFVbGc6DURpBB-rFvr_cbr22amSMGi1v6RcfXNcbEgZdUFEbjyEwubuTUAo_7mW8Dxr3Zohr2CCTIxMtxdjrtGvcdQPT0_E25Bw8bJxMmOTQw662hpb9XQS9xnobuR6qyBfIA7oHaJV3RTFz0lU1s1i0qtSCqtqxpqEhA8BkalvKIdzh7_cuycXnY3VbF4BOAOZxwvQ1rrscqjXQdr6cqkZetZDSE2g5_5LB1Ki60IA_5WCQBg";
  private static final String JWK = "{\"keys\":[{\"alg\":\"RS256\",\"e\":\"AQAB\",\"kid\":\"3e6hWRotCd1nqDE5jwzaHW5AELzm2TPl5ADJPM2OOYQ=\",\"kty\":\"RSA\",\"n\":\"0cripNZ9mj_4zuUGkESqZAw2IU2FvmTGdZHk2IxxoAdzz5ESpkqA5UbSoqVm7avAmNf-8jNWaQfjtCPphHVrsjAKqKgUc_haL4kkO5PS_78wCXnq_Yr7d4OyQexD1hgoiI-Ic89Q8R3px1OE1RVVwA815Fvp5OqfVQJc-rpoPaI1rJM02XrpOlKd-PN_bAqYXbpNtwU-kB85ieeoLt3N4EI1RpBB8ZfzWp0YeLYN5jBTRGalmcplQGBTlXJPtMzjLT76cjEgcRVRNZSMWApma-XrWo7UQDdzGZb56_ba2Nx-cSytLwNVyvXeJIcMkKrnu3Gs2jg_jQ5GQbjf3ohygQ\",\"use\":\"sig\"},{\"alg\":\"RS256\",\"e\":\"AQAB\",\"kid\":\"TzkAfSHjD1XItTV3X/XKei6STtUHw0zgjc8NB5g3RxA=\",\"kty\":\"RSA\",\"n\":\"1KPkC5PA9n9g8uYFOaFdR7QgndxSBMYUxQApjrfh-7TCeKBTcW55uHz8FBa2YD3Kl7ei86Zyx7xdcPndouutZIfqMD5Ff6xbXeZ2Q-OCj-3RgnWU-Ie5DI--VjjslG-NlRZYDLpIDyDqho_bRIQ_tuemxDJc_-jJVFjGHUKKsLC6BYifGylqgyILJWjFYadWOoqr4EWE-pvng8bUnT6a1nZTjNtfGUdabp_pHxvi6LWVAeOKkrswZ0vMlKxThh46v0ZsQwqyjjxeSnRBqh_z9wtSleUXfRAXoLZdBpmfxBWoCk7CiGdeaThVx_Amw6qGYK1uX60vtNjRJRDMqy2d3Q\",\"use\":\"sig\"}]}";
  private static final String MISMATCHED_JWK = "{\"keys\": [  {    \"alg\": \"RS256\",    \"kty\": \"RSA\",    \"use\": \"sig\",    \"x5c\": [      \"MIIC+DCCAeCgAwIBAgIJBIGjYW6hFpn2MA0GCSqGSIb3DQEBBQUAMCMxITAfBgNVBAMTGGN1c3RvbWVyLWRlbW9zLmF1dGgwLmNvbTAeFw0xNjExMjIyMjIyMDVaFw0zMDA4MDEyMjIyMDVaMCMxITAfBgNVBAMTGGN1c3RvbWVyLWRlbW9zLmF1dGgwLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMnjZc5bm/eGIHq09N9HKHahM7Y31P0ul+A2wwP4lSpIwFrWHzxw88/7Dwk9QMc+orGXX95R6av4GF+Es/nG3uK45ooMVMa/hYCh0Mtx3gnSuoTavQEkLzCvSwTqVwzZ+5noukWVqJuMKNwjL77GNcPLY7Xy2/skMCT5bR8UoWaufooQvYq6SyPcRAU4BtdquZRiBT4U5f+4pwNTxSvey7ki50yc1tG49Per/0zA4O6Tlpv8x7Red6m1bCNHt7+Z5nSl3RX/QYyAEUX1a28VcYmR41Osy+o2OUCXYdUAphDaHo4/8rbKTJhlu8jEcc1KoMXAKjgaVZtG/v5ltx6AXY0CAwEAAaMvMC0wDAYDVR0TBAUwAwEB/zAdBgNVHQ4EFgQUQxFG602h1cG+pnyvJoy9pGJJoCswDQYJKoZIhvcNAQEFBQADggEBAGvtCbzGNBUJPLICth3mLsX0Z4z8T8iu4tyoiuAshP/Ry/ZBnFnXmhD8vwgMZ2lTgUWwlrvlgN+fAtYKnwFO2G3BOCFw96Nm8So9sjTda9CCZ3dhoH57F/hVMBB0K6xhklAc0b5ZxUpCIN92v/w+xZoz1XQBHe8ZbRHaP1HpRM4M7DJk2G5cgUCyu3UBvYS41sHvzrxQ3z7vIePRA4WF4bEkfX12gvny0RsPkrbVMXX1Rj9t6V7QXrbPYBAO+43JvDGYawxYVvLhz+BJ45x50GFQmHszfY3BR9TPK8xmMmQwtIvLu1PMttNCs7niCYkSiUv2sc2mlq1i3IashGkkgmo=\"    ],    \"n\": \"yeNlzlub94YgerT030codqEztjfU_S6X4DbDA_iVKkjAWtYfPHDzz_sPCT1Axz6isZdf3lHpq_gYX4Sz-cbe4rjmigxUxr-FgKHQy3HeCdK6hNq9ASQvMK9LBOpXDNn7mei6RZWom4wo3CMvvsY1w8tjtfLb-yQwJPltHxShZq5-ihC9irpLI9xEBTgG12q5lGIFPhTl_7inA1PFK97LuSLnTJzW0bj096v_TMDg7pOWm_zHtF53qbVsI0e3v5nmdKXdFf9BjIARRfVrbxVxiZHjU6zL6jY5QJdh1QCmENoejj_ytspMmGW7yMRxzUqgxcAqOBpVm0b-_mW3HoBdjQ\",    \"e\": \"AQAB\",    \"kid\": \"TzkAfSHjD1XItTV3X/XKei6STtUHw0zgjc8NB5g3RxA=\",    \"x5t\": \"NjVBRjY5MDlCMUIwNzU4RTA2QzZFMDQ4QzQ2MDAyQjVDNjk1RTM2Qg\"  }]}";
  private static final String MISSING_KEY_JWK = "{\"keys\":[{\"alg\":\"RS256\",\"e\":\"AQAB\",\"kid\":\"1234567\",\"kty\":\"RSA\",\"n\":\"0cripNZ9mj_4zuUGkESqZAw2IU2FvmTGdZHk2IxxoAdzz5ESpkqA5UbSoqVm7avAmNf-8jNWaQfjtCPphHVrsjAKqKgUc_haL4kkO5PS_78wCXnq_Yr7d4OyQexD1hgoiI-Ic89Q8R3px1OE1RVVwA815Fvp5OqfVQJc-rpoPaI1rJM02XrpOlKd-PN_bAqYXbpNtwU-kB85ieeoLt3N4EI1RpBB8ZfzWp0YeLYN5jBTRGalmcplQGBTlXJPtMzjLT76cjEgcRVRNZSMWApma-XrWo7UQDdzGZb56_ba2Nx-cSytLwNVyvXeJIcMkKrnu3Gs2jg_jQ5GQbjf3ohygQ\",\"use\":\"sig\"},{\"alg\":\"RS256\",\"e\":\"AQAB\",\"kid\":\"891011\",\"kty\":\"RSA\",\"n\":\"1KPkC5PA9n9g8uYFOaFdR7QgndxSBMYUxQApjrfh-7TCeKBTcW55uHz8FBa2YD3Kl7ei86Zyx7xdcPndouutZIfqMD5Ff6xbXeZ2Q-OCj-3RgnWU-Ie5DI--VjjslG-NlRZYDLpIDyDqho_bRIQ_tuemxDJc_-jJVFjGHUKKsLC6BYifGylqgyILJWjFYadWOoqr4EWE-pvng8bUnT6a1nZTjNtfGUdabp_pHxvi6LWVAeOKkrswZ0vMlKxThh46v0ZsQwqyjjxeSnRBqh_z9wtSleUXfRAXoLZdBpmfxBWoCk7CiGdeaThVx_Amw6qGYK1uX60vtNjRJRDMqy2d3Q\",\"use\":\"sig\"}]}";
  private static final JwtValidator JWT_VALIDATOR = new JwtValidatorImpl();

  @Test()
  public void verifyTokenExpired() throws ParseException, IOException, JOSEException {
    final String jwkBase64 = Base64.getEncoder().encodeToString(JWK.getBytes());
    assertFalse(JWT_VALIDATOR.jwtIsValid(EXPIRED_M2M_JWT, jwkBase64));
  }

  @Test()
  public void verifyMissingJwk() throws ParseException, IOException, JOSEException {
    final String jwkBase64 = Base64.getEncoder().encodeToString(MISSING_KEY_JWK.getBytes());
    assertFalse(JWT_VALIDATOR.jwtIsValid(EXPIRED_M2M_JWT, jwkBase64));
  }

  @Test()
  public void verifyInvalidJwk() throws ParseException, IOException, JOSEException {
    final String jwkBase64 = Base64.getEncoder().encodeToString(MISMATCHED_JWK.getBytes());
    assertFalse(JWT_VALIDATOR.jwtIsValid(EXPIRED_M2M_JWT, jwkBase64));
  }

  @Test()
  public void verifyTokenNotValid() {
    assertThrows(ParseException.class, () -> {
      final String jwkBase64 = Base64.getEncoder().encodeToString(JWK.getBytes());
      JWT_VALIDATOR.jwtIsValid("blah", jwkBase64);
    });
  }
}
