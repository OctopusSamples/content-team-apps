package com.octopus.products.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.products.BaseTest;
import com.octopus.products.domain.entities.Product;
import com.octopus.products.infrastructure.utilities.LiquidbaseUpdater;
import com.octopus.exceptions.UnauthorizedException;
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
public class HandlerAuthorizedPostTests extends BaseTest {

  @Inject
  LiquidbaseUpdater liquidbaseUpdater;

  @Inject
  ResourceHandlerCreate handler;

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
    assertThrows(UnauthorizedException.class, () -> handler.create(
        resourceToResourceDocument(resourceConverter, new Product()),
        List.of("main"),
        null, null));
  }
}
