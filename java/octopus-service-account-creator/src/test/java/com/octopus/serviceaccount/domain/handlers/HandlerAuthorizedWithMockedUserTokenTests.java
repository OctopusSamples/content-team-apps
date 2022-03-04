package com.octopus.serviceaccount.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import com.octopus.serviceaccount.BaseTest;
import com.octopus.serviceaccount.domain.entities.ServiceAccount;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * Simulate tests when a user token has been passed in.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerAuthorizedWithMockedUserTokenTests extends BaseTest {

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @InjectMock
  JwtInspector jwtInspector;

  @InjectMock
  JwtUtils jwtUtils;

  @Inject
  ServiceAccountHandler serviceAccountHandler;

  @Inject
  ResourceConverter resourceConverter;

  @BeforeAll
  public void setup() {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(false);
    Mockito.when(jwtUtils.getJwtFromAuthorizationHeader(any())).thenReturn(Optional.of(""));
    Mockito.when(jwtInspector.jwtContainsCognitoGroup(any(), any())).thenReturn(true);
  }

  @Test
  @Transactional
  public void testCreateAudit() throws DocumentSerializationException {
    final ServiceAccount audit = createResource(serviceAccountHandler, resourceConverter);
    assertEquals("myname", audit.getUsername());
  }
}
