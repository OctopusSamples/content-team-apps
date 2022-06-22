package com.octopus.audits.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.audits.BaseTest;
import com.octopus.audits.domain.entities.Audit;
import com.octopus.audits.domain.exceptions.Unauthorized;
import com.octopus.audits.domain.features.impl.DisableSecurityFeature;
import com.octopus.audits.infrastructure.utilities.LiquidbaseUpdater;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.sql.SQLException;
import java.util.List;
import javax.inject.Inject;
import javax.transaction.Transactional;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerAuthorizedTests extends BaseTest {

  @Inject
  LiquidbaseUpdater liquidbaseUpdater;

  @Inject
  AuditsHandler auditsHandler;

  @Inject
  ResourceConverter resourceConverter;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @BeforeAll
  public void setup() throws SQLException, LiquibaseException {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(false);
    liquidbaseUpdater.update();
  }

  @Test
  @Transactional
  public void testCreateAudit() {
    assertThrows(Unauthorized.class, () -> auditsHandler.create(
        auditToResourceDocument(resourceConverter, new Audit()),
        List.of("main"),
        null, null));
  }

  @Test
  @Transactional
  public void testGetAudit() {
    assertThrows(Unauthorized.class, () -> auditsHandler.getOne(
        "1",
        List.of("main"),
        null, null));
  }

  @Test
  @Transactional
  public void testGetAllAudit() {
    assertThrows(Unauthorized.class, () -> auditsHandler.getAll(
        List.of("main"),
        null, null, null, null, null));
  }

}
