package com.octopus.audits.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.audits.BaseTest;
import com.octopus.audits.domain.entities.Audit;
import com.octopus.audits.domain.utilities.DisableSecurityFeature;
import com.octopus.audits.domain.utilities.JwtUtils;
import com.octopus.audits.domain.utilities.impl.JoseJwtVerifier;
import com.octopus.audits.infrastructure.utilities.LiquidbaseUpdater;
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
 * Simulate tests when a user token has been passed in.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerAuthorizedWithMockedUserTokenTests extends BaseTest {

  @Inject
  LiquidbaseUpdater liquidbaseUpdater;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @InjectMock
  JoseJwtVerifier jwtVerifier;

  @InjectMock
  JwtUtils jwtUtils;

  @Inject
  AuditsHandler auditsHandler;

  @Inject
  ResourceConverter resourceConverter;

  @BeforeAll
  public void setup() throws SQLException, LiquibaseException {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(false);
    Mockito.when(jwtUtils.getJwtFromAuthorizationHeader(any())).thenReturn(Optional.of(""));
    Mockito.when(jwtVerifier.jwtContainsCognitoGroup(any(), any())).thenReturn(true);
    liquidbaseUpdater.update();
  }

  @Test
  @Transactional
  public void testCreateAudit() throws DocumentSerializationException {
    final Audit audit = createAudit( auditsHandler, resourceConverter, "main");
    assertEquals("myname", audit.getSubject());
  }
}
