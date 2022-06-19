package com.octopus.products.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.products.BaseTest;
import com.octopus.products.domain.entities.Product;
import com.octopus.products.infrastructure.utilities.LiquidbaseUpdater;
import io.quarkus.test.junit.QuarkusTest;
import java.sql.SQLException;
import java.util.List;
import javax.inject.Inject;
import javax.transaction.Transactional;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * These tests are focused on the creation of new resources through POST operations.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerPostTests extends BaseTest {
  
  @Inject
  LiquidbaseUpdater liquidbaseUpdater;

  @Inject
  ResourceHandlerCreate handler;

  @Inject
  ResourceConverter resourceConverter;

  @BeforeAll
  public void setup() throws SQLException, LiquibaseException {
    liquidbaseUpdater.update();
  }

  @Test
  @Transactional
  public void createResourceTestNull() {
    assertThrows(NullPointerException.class, () -> {
      handler.create(
          null,
          List.of("testing"),
          null,
          null);
    });

    assertThrows(NullPointerException.class, () -> {
      final Product resource = createResource("subject");
      handler.create(resourceToResourceDocument(resourceConverter, resource),
          null,
          null,
          null);
    });
  }

  @Test
  @Transactional
  public void testCreateResource() throws DocumentSerializationException {
    final Product resultObject = createResource(handler, resourceConverter, "testing");
    assertNotNull(resultObject.getId());
    assertEquals("testing", resultObject.getDataPartition());
    assertEquals("myname", resultObject.getName());
    assertEquals("http://example.org/pdf", resultObject.getPdf());
    assertEquals("http://example.org/image", resultObject.getImage());
    assertEquals("http://example.org/epub", resultObject.getEpub());
  }
}
