package com.octopus.products.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.products.BaseTest;
import com.octopus.products.domain.entities.Product;
import com.octopus.products.infrastructure.utilities.LiquidbaseUpdater;
import com.octopus.exceptions.EntityNotFoundException;
import io.quarkus.test.junit.QuarkusTest;
import java.sql.SQLException;
import java.util.List;
import javax.inject.Inject;
import javax.transaction.Transactional;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * These tests are mostly focused on the retrieval of new resources through GET operations.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerGetTests extends BaseTest {
  
  @Inject
  LiquidbaseUpdater liquidbaseUpdater;

  @Inject
  ResourceHandlerGetAll handler;

  @Inject
  ResourceHandlerGetOne handlerGetOne;

  @Inject
  ResourceHandlerCreate handlerCreate;

  @Inject
  HealthHandler healthHandler;

  @Inject
  ResourceConverter resourceConverter;

  @BeforeAll
  public void setup() throws SQLException, LiquibaseException {
    liquidbaseUpdater.update();
  }

  @Test
  @Transactional
  public void getResourceTestNull() {
    assertThrows(NullPointerException.class, () -> {
      handler.getAll(
          null,
          null,
          null,
          null,
          null,
          null);
    });
  }

  @Test
  @Transactional
  public void getOneResourceTestNull() {
    assertThrows(NullPointerException.class, () -> {
      handlerGetOne.getOne(
          null,
          List.of("testing"),
          null,
          null);
    });

    assertThrows(NullPointerException.class, () -> {
      handlerGetOne.getOne(
          "1",
          null,
          null,
          null);
    });
  }

  @Test
  @Transactional
  public void getMissingResource() {
    assertThrows(EntityNotFoundException.class, () ->
        handlerGetOne.getOne(
          "1000000000000000000",
          List.of("main"),
          null, null)
    );
  }

  /**
   * You should not be able to get a resource in another partition.
   *
   * @param partition The partition to use when retrieving
   * @throws DocumentSerializationException
   */
  @ParameterizedTest
  @Transactional
  @ValueSource(strings = {"testing2", "", " ", "main", " main ", " testing2 "})
  public void failGetResource(final String partition) throws DocumentSerializationException {
    final Product resource = createResource("subject");
    final String result =
        handlerCreate.create(
            resourceToResourceDocument(resourceConverter, resource),
            List.of("testing"),
            null, null);
    final Product resultObject = getResourceFromDocument(resourceConverter, result);

    assertThrows(
        EntityNotFoundException.class,
        () ->
            handlerGetOne.getOne(
                resultObject.getId().toString(),
                List.of("" + partition),
                null, null));

    assertThrows(
        EntityNotFoundException.class,
        () -> handlerGetOne.getOne(resultObject.getId().toString(), List.of(), null, null));
  }

}
