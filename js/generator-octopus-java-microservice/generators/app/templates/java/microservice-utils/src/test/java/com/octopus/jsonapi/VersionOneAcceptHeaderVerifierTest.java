package com.octopus.jsonapi;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.octopus.Constants;
import com.octopus.exceptions.InvalidAcceptHeadersException;
import com.octopus.jsonapi.impl.VersionOneAcceptHeaderVerifier;
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
    VERSION_ONE_ACCEPT_HEADER_VERIFIER.checkAcceptHeader(List.of(Constants.JsonApi.JSONAPI_CONTENT_TYPE));
  }

  @Test
  public void verifyMixedPlainJsonAPIAcceptHeaders() {
    VERSION_ONE_ACCEPT_HEADER_VERIFIER.checkAcceptHeader(List.of(
        Constants.JsonApi.JSONAPI_CONTENT_TYPE,
        Constants.JsonApi.JSONAPI_CONTENT_TYPE + "; version=2"));
  }

  @Test
  public void verifyBadJsonAPIAcceptHeaders() {
    assertThrows(InvalidAcceptHeadersException.class, () -> {
      VERSION_ONE_ACCEPT_HEADER_VERIFIER.checkAcceptHeader(
          List.of(Constants.JsonApi.JSONAPI_CONTENT_TYPE + "; version=2"));
    });
  }
}
