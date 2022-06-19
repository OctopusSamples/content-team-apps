package com.octopus.products.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * These tests are mostly focused on the creation and retrieval of new resources through POST and GET operations.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerGetPostTests extends BaseTest {
  
  @Inject
  LiquidbaseUpdater liquidbaseUpdater;

  @Inject
  ResourceHandlerCreate handlerCreate;

  @Inject
  ResourceHandlerGetOne handlerGetOne;

  @Inject
  ResourceHandlerGetAll handlerGetAll;

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
      handlerCreate.create(
          null,
          List.of("testing"),
          null,
          null);
    });

    assertThrows(NullPointerException.class, () -> {
      final Product resource = createResource("subject");
      handlerCreate.create(resourceToResourceDocument(resourceConverter, resource),
          null,
          null,
          null);
    });
  }

  @Test
  @Transactional
  public void testCreateResource() throws DocumentSerializationException {
    final Product resultObject = createResource(handlerCreate, resourceConverter, "testing");
    assertNotNull(resultObject.getId());
    assertEquals("testing", resultObject.getDataPartition());
    assertEquals("myname", resultObject.getName());
    assertEquals("http://example.org/pdf", resultObject.getPdf());
    assertEquals("http://example.org/image", resultObject.getImage());
    assertEquals("http://example.org/epub", resultObject.getEpub());
  }

  @Test
  @Transactional
  public void getResource() throws DocumentSerializationException {
    final Product resource = createResource("subject");
    final String result =
        handlerCreate.create(
            resourceToResourceDocument(resourceConverter, resource),
            List.of("testing"),
            null, null);
    final Product resultObject = getResourceFromDocument(resourceConverter, result);

    final String getResult =
        handlerGetOne.getOne(
            resultObject.getId().toString(),
            List.of("testing"),
            null, null);
    final Product getResultObject = getResourceFromDocument(resourceConverter, getResult);

    assertEquals(resultObject.getId(), getResultObject.getId());
    assertEquals(resultObject.getName(), getResultObject.getName());
    assertEquals(resultObject.getPdf(), getResultObject.getPdf());
    assertEquals(resultObject.getImage(), getResultObject.getImage());
    assertEquals(resultObject.getEpub(), getResultObject.getEpub());
  }


  @Test
  @Transactional
  public void getAllResource() throws DocumentSerializationException {
    final Product resource = createResource("subject");
    final String result =
        handlerCreate.create(
            resourceToResourceDocument(resourceConverter, resource),
            List.of("testing"),
            null, null);
    final Product resultObject = getResourceFromDocument(resourceConverter, result);

    final String getResult =
        handlerGetAll.getAll(
            List.of("testing"),
            "id==" + resultObject.getId(),
            null,
            null,
            null,
            null);
    final List<Product> getResultObjects = getResourcesFromDocument(resourceConverter, getResult);

    assertEquals(1, getResultObjects.size());
    assertEquals(resultObject.getId(), getResultObjects.get(0).getId());
    assertEquals(resultObject.getName(), getResultObjects.get(0).getName());
    assertEquals(resultObject.getPdf(), getResultObjects.get(0).getPdf());
    assertEquals(resultObject.getImage(), getResultObjects.get(0).getImage());
    assertEquals(resultObject.getEpub(), getResultObjects.get(0).getEpub());
    assertEquals(resultObject.getDataPartition(), getResultObjects.get(0).getDataPartition());
  }



  /**
   * You should not be able to list resources in another partition.
   *
   * @param partition The partition to use when retrieving
   * @throws DocumentSerializationException
   */
  @ParameterizedTest
  @Transactional
  @ValueSource(strings = {"testing2", "", " ", "main", " main ", " testing2 "})
  public void failGetResources(final String partition) throws DocumentSerializationException {
    final Product resource = createResource("subject");
    final String result =
        handlerCreate.create(
            resourceToResourceDocument(resourceConverter, resource),
            List.of("testing"),
            null, null);
    final Product resultObject = getResourceFromDocument(resourceConverter, result);

    final String getResult =
        handlerGetAll.getAll(
            List.of("" + partition),
            "",
            null,
            null,
            null,
            null);
    final List<Product> getResultObjects = getResourcesFromDocument(resourceConverter, getResult);

    assertFalse(getResultObjects.stream().anyMatch(p -> p.getId().equals(resultObject.getId())));

    final String getResult2 = handlerGetAll.getAll(
        List.of(),
        "",
        null,
        null,
        null,
        null);
    final List<Product> getResultObjects2 = getResourcesFromDocument(resourceConverter, getResult2);

    assertFalse(getResultObjects2.stream().anyMatch(p -> p.getId().equals(resultObject.getId())));
  }
}
