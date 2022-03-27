package com.octopus.customers.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.customers.BaseTest;
import com.octopus.customers.domain.entities.Customer;
import com.octopus.customers.infrastructure.utilities.LiquidbaseUpdater;
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
 * These tests are mostly focused on the creation of new resources through POST operations.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerPostTests extends BaseTest {
  
  @Inject
  LiquidbaseUpdater liquidbaseUpdater;

  @Inject
  ResourceHandler handler;

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
  public void createResourceTestNull() {
    assertThrows(NullPointerException.class, () -> {
      handler.create(
          null,
          List.of("testing"),
          null,
          null);
    });

    assertThrows(NullPointerException.class, () -> {
      final Customer resource = createResource("subject");
      handler.create(resourceToResourceDocument(resourceConverter, resource),
          null,
          null,
          null);
    });
  }

  @Test
  @Transactional
  public void testCreateResource() throws DocumentSerializationException {
    final Customer resultObject = createResource(handler, resourceConverter, "testing");
    assertNotNull(resultObject.getId());
    assertEquals("testing", resultObject.getDataPartition());
    assertEquals("myname", resultObject.getFirstName());
    assertEquals("Doe", resultObject.getLastName());
    assertEquals("1 Octopus St", resultObject.getAddressLine1());
    assertEquals("Coral Garden", resultObject.getAddressLine2());
    assertEquals("Brisbane", resultObject.getCity());
  }

  @Test
  @Transactional
  public void getResource() throws DocumentSerializationException {
    final Customer resource = createResource("subject");
    final String result =
        handler.create(
            resourceToResourceDocument(resourceConverter, resource),
            List.of("testing"),
            null, null);
    final Customer resultObject = getResourceFromDocument(resourceConverter, result);

    final String getResult =
        handler.getOne(
            resultObject.getId().toString(),
            List.of("testing"),
            null, null);
    final Customer getResultObject = getResourceFromDocument(resourceConverter, getResult);

    assertEquals(resultObject.getId(), getResultObject.getId());
    assertEquals(resultObject.getFirstName(), getResultObject.getFirstName());
    assertEquals(resultObject.getLastName(), getResultObject.getLastName());
    assertEquals(resultObject.getAddressLine1(), getResultObject.getAddressLine1());
    assertEquals(resultObject.getAddressLine2(), getResultObject.getAddressLine2());
    assertEquals(resultObject.getCity(), getResultObject.getCity());
  }


  @Test
  @Transactional
  public void getAllResource() throws DocumentSerializationException {
    final Customer resource = createResource("subject");
    final String result =
        handler.create(
            resourceToResourceDocument(resourceConverter, resource),
            List.of("testing"),
            null, null);
    final Customer resultObject = getResourceFromDocument(resourceConverter, result);

    final String getResult =
        handler.getAll(
            List.of("testing"),
            "id==" + resultObject.getId(),
            null,
            null,
            null,
            null);
    final List<Customer> getResultObjects = getResourcesFromDocument(resourceConverter, getResult);

    assertEquals(1, getResultObjects.size());
    assertEquals(resultObject.getId(), getResultObjects.get(0).getId());
    assertEquals(resultObject.getFirstName(), getResultObjects.get(0).getFirstName());
    assertEquals(resultObject.getLastName(), getResultObjects.get(0).getLastName());
    assertEquals(resultObject.getAddressLine1(), getResultObjects.get(0).getAddressLine1());
    assertEquals(resultObject.getAddressLine2(), getResultObjects.get(0).getAddressLine2());
    assertEquals(resultObject.getCity(), getResultObjects.get(0).getCity());
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
    final Customer resource = createResource("subject");
    final String result =
        handler.create(
            resourceToResourceDocument(resourceConverter, resource),
            List.of("testing"),
            null, null);
    final Customer resultObject = getResourceFromDocument(resourceConverter, result);

    final String getResult =
        handler.getAll(
            List.of("" + partition),
            "",
            null,
            null,
            null,
            null);
    final List<Customer> getResultObjects = getResourcesFromDocument(resourceConverter, getResult);

    assertFalse(getResultObjects.stream().anyMatch(p -> p.getId().equals(resultObject.getId())));

    final String getResult2 = handler.getAll(
        List.of(),
        "",
        null,
        null,
        null,
        null);
    final List<Customer> getResultObjects2 = getResourcesFromDocument(resourceConverter, getResult2);

    assertFalse(getResultObjects2.stream().anyMatch(p -> p.getId().equals(resultObject.getId())));
  }
}
