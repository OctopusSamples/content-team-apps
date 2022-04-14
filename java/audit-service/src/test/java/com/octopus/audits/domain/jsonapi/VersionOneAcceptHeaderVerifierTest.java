package com.octopus.audits.domain.jsonapi;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.octopus.audits.GlobalConstants;
import com.octopus.audits.domain.Constants;
import com.octopus.audits.domain.exceptions.InvalidAcceptHeaders;
import com.octopus.audits.domain.jsonapi.impl.VersionOneAcceptHeaderVerifier;
import java.util.List;
import org.junit.jupiter.api.Test;

public class VersionOneAcceptHeaderVerifierTest {
  private static final VersionOneAcceptHeaderVerifier VERSION_ONE_ACCEPT_HEADER_VERIFIER = new VersionOneAcceptHeaderVerifier();

  @Test
  public void verifyNoAcceptHeaders() {
    VERSION_ONE_ACCEPT_HEADER_VERIFIER.checkAcceptHeader(List.of());
    VERSION_ONE_ACCEPT_HEADER_VERIFIER.checkAcceptHeader(null);
  }

  @Test
  public void verifyNoJsonAPIAcceptHeaders() {
    VERSION_ONE_ACCEPT_HEADER_VERIFIER.checkAcceptHeader(List.of("*/*"));
  }

  @Test
  public void verifyOnePlainJsonAPIAcceptHeader() {
    VERSION_ONE_ACCEPT_HEADER_VERIFIER.checkAcceptHeader(List.of(GlobalConstants.JSONAPI_CONTENT_TYPE));
  }

  @Test
  public void verifyMixedPlainJsonAPIAcceptHeaders() {
    VERSION_ONE_ACCEPT_HEADER_VERIFIER.checkAcceptHeader(List.of(
        GlobalConstants.JSONAPI_CONTENT_TYPE,
        GlobalConstants.JSONAPI_CONTENT_TYPE + "; version=2"));
  }

  @Test
  public void verifyBadJsonAPIAcceptHeaders() {
    assertThrows(InvalidAcceptHeaders.class, () -> {
      VERSION_ONE_ACCEPT_HEADER_VERIFIER.checkAcceptHeader(
          List.of(GlobalConstants.JSONAPI_CONTENT_TYPE + "; version=2"));
    });
  }
}
