package com.octopus.customers.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.customers.BaseTest;
import com.octopus.customers.domain.entities.Customer;
import com.octopus.customers.infrastructure.utilities.LiquidbaseUpdater;
import com.octopus.exceptions.Unauthorized;
import com.octopus.features.DisableSecurityFeature;
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
  CustomersHandler handler;

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
  public void testCreateResource() {
    assertThrows(Unauthorized.class, () -> handler.create(
        resourceToResourceDocument(resourceConverter, new Customer()),
        List.of("main"),
        null, null));
  }

  @Test
  @Transactional
  public void testGetResource() {
    assertThrows(Unauthorized.class, () -> handler.getOne(
        "1",
        List.of("main"),
        null, null));
  }

  @Test
  @Transactional
  public void testGetAllResource() {
    assertThrows(Unauthorized.class, () -> handler.getAll(
        List.of("main"),
        null, null, null, null, null));
  }

}
