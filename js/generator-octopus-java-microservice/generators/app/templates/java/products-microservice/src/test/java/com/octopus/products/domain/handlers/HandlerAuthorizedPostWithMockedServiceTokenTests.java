package com.octopus.products.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.products.BaseTest;
import com.octopus.products.domain.entities.Product;
import com.octopus.products.infrastructure.utilities.LiquidbaseUpdater;
import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.sql.SQLException;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * Simulate tests when a machine-to-machine token has been passed in.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerAuthorizedPostWithMockedServiceTokenTests extends BaseTest {

  @Inject
  LiquidbaseUpdater liquidbaseUpdater;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @InjectMock
  AdminJwtClaimFeature cognitoAdminClaim;

  @InjectMock
  JwtInspector jwtInspector;

  @InjectMock
  JwtUtils jwtUtils;

  @Inject
  ResourceHandlerCreate handler;

  @Inject
  ResourceConverter resourceConverter;

  @BeforeAll
  public void setup() throws SQLException, LiquibaseException {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(false);
    Mockito.when(jwtUtils.getJwtFromAuthorizationHeader(any())).thenReturn(Optional.of(""));
    Mockito.when(jwtInspector.jwtContainsScope(any(), any(), any())).thenReturn(true);
    Mockito.when(cognitoAdminClaim.getAdminClaim()).thenReturn(Optional.of("admin-claim"));
    liquidbaseUpdater.update();
  }

  @Test
  @Transactional
  public void testCreateResource() throws DocumentSerializationException {
    final Product resource = createResource(handler, resourceConverter, "main");
    assertEquals("myname", resource.getName());
  }
}
