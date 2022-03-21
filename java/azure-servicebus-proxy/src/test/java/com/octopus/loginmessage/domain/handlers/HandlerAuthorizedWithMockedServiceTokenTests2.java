package com.octopus.loginmessage.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import com.octopus.loginmessage.BaseTest;
import com.octopus.loginmessage.CommercialAzureServiceBusTestProfile;
import com.octopus.loginmessage.infrastructure.octofront.CommercialServiceBus;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * Simulate tests when a machine-to-machine token has been passed in.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(CommercialAzureServiceBusTestProfile.class)
public class HandlerAuthorizedWithMockedServiceTokenTests2 extends BaseTest {

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @InjectMock
  AdminJwtClaimFeature cognitoAdminClaim;

  @InjectMock
  JwtInspector jwtInspector;

  @InjectMock
  JwtUtils jwtUtils;

  @Inject
  ResourceHandler handler;

  @Inject
  ResourceConverter resourceConverter;

  @InjectMock
  CommercialServiceBus commercialServiceBus;

  @BeforeAll
  public void setup() {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(false);
    Mockito.when(jwtUtils.getJwtFromAuthorizationHeader(any())).thenReturn(Optional.empty());
    Mockito.when(jwtInspector.jwtContainsScope(any(), any(), any())).thenReturn(true);
    Mockito.when(cognitoAdminClaim.getAdminClaim()).thenReturn(Optional.empty());
    Mockito.doNothing().when(commercialServiceBus).sendUserDetails(any(), any());
  }

  @Test
  @Transactional
  public void testCreateResource() {
    assertThrows(
        UnauthorizedException.class,
        () -> createResource(handler, resourceConverter, "main"),
        "Should be unauthorized because AdminJwtClaimFeature.getAdminClaim returned an empty optional");
  }
}
