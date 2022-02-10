package com.octopus.audits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.audits.domain.entities.Audit;
import com.octopus.audits.domain.exceptions.EntityNotFound;
import com.octopus.audits.domain.handlers.HealthHandler;
import com.octopus.audits.domain.handlers.AuditsHandler;
import com.octopus.audits.infrastructure.utilities.LiquidbaseUpdater;
import io.quarkus.test.junit.QuarkusTest;
import java.sql.SQLException;
import java.util.List;
import javax.inject.Inject;
import javax.transaction.Transactional;
import liquibase.exception.LiquibaseException;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerTests extends BaseTest {

  @Inject LiquidbaseUpdater liquidbaseUpdater;

  @Inject
  AuditsHandler auditsHandler;

  @Inject HealthHandler healthHandler;

  @Inject ResourceConverter resourceConverter;

  @BeforeAll
  public void setup() throws SQLException, LiquibaseException {
    liquidbaseUpdater.update();
  }

  @ParameterizedTest
  @CsvSource({
    "/health/audits,GET",
    "/health/audits,POST",
    "/health/audits/x,GET"
  })
  public void testHealth(@NonNull final String path, @NonNull final String method)
      throws DocumentSerializationException {
    assertNotNull(healthHandler.getHealth(path, method));
  }

  @Test
  @Transactional
  public void testCreateAudit() throws DocumentSerializationException {
    final Audit resultObject = createAudit(auditsHandler, resourceConverter, "testing");
    assertNotNull(resultObject.getId());
    assertEquals("testing", resultObject.getDataPartition());
    assertEquals("myname", resultObject.getSubject());
    assertEquals("object", resultObject.getObject());
    assertEquals("action", resultObject.getAction());
  }

  @Test
  @Transactional
  public void getAudit() throws DocumentSerializationException {
    final Audit audit = createAudit("subject");
    final String result =
        auditsHandler.create(
            auditToResourceDocument(resourceConverter, audit),
            List.of("application/vnd.api+json; dataPartition=testing"),
            null);
    final Audit resultObject = getAuditFromDocument(resourceConverter, result);

    final String getResult =
        auditsHandler.getOne(
            resultObject.getId().toString(),
            List.of("application/vnd.api+json; dataPartition=testing"),
            null);
    final Audit getResultObject = getAuditFromDocument(resourceConverter, getResult);

    assertEquals(resultObject.getId(), getResultObject.getId());
    assertEquals(resultObject.getObject(), getResultObject.getObject());
    assertEquals(resultObject.getSubject(), getResultObject.getSubject());
    assertEquals(resultObject.getDataPartition(), getResultObject.getDataPartition());
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
  public void failGetAudit(final String partition) throws DocumentSerializationException {
    final Audit audit = createAudit("subject");
    final String result =
        auditsHandler.create(
            auditToResourceDocument(resourceConverter, audit),
            List.of("application/vnd.api+json; dataPartition=testing"),
            null);
    final Audit resultObject = getAuditFromDocument(resourceConverter, result);

    assertThrows(
        EntityNotFound.class,
        () ->
            auditsHandler.getOne(
                resultObject.getId().toString(),
                List.of("application/vnd.api+json; dataPartition=" + partition),
                null));

    assertThrows(
        EntityNotFound.class,
        () -> auditsHandler.getOne(resultObject.getId().toString(), List.of(), null));
  }

  @Test
  @Transactional
  public void getAllAudit() throws DocumentSerializationException {
    final Audit audit = createAudit("subject");
    final String result =
        auditsHandler.create(
            auditToResourceDocument(resourceConverter, audit),
            List.of("application/vnd.api+json; dataPartition=testing"),
            null);
    final Audit resultObject = getAuditFromDocument(resourceConverter, result);

    final String getResult =
        auditsHandler.getAll(
            List.of("application/vnd.api+json; dataPartition=testing"),
            "id==" + resultObject.getId(), null);
    final List<Audit> getResultObjects = getAuditsFromDocument(resourceConverter, getResult);

    assertEquals(1, getResultObjects.size());
    assertEquals(resultObject.getId(), getResultObjects.get(0).getId());
    assertEquals(resultObject.getObject(), getResultObjects.get(0).getObject());
    assertEquals(resultObject.getSubject(), getResultObjects.get(0).getSubject());
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
  public void failGetAudits(final String partition) throws DocumentSerializationException {
    final Audit audit = createAudit("subject");
    final String result =
        auditsHandler.create(
            auditToResourceDocument(resourceConverter, audit),
            List.of("application/vnd.api+json; dataPartition=testing"),
            null);
    final Audit resultObject = getAuditFromDocument(resourceConverter, result);

    final String getResult =
        auditsHandler.getAll(List.of("application/vnd.api+json; dataPartition=" + partition), "", null);
    final List<Audit> getResultObjects = getAuditsFromDocument(resourceConverter, getResult);

    assertFalse(getResultObjects.stream().anyMatch(p -> p.getId().equals(resultObject.getId())));

    final String getResult2 = auditsHandler.getAll(List.of(), "", null);
    final List<Audit> getResultObjects2 = getAuditsFromDocument(resourceConverter, getResult2);

    assertFalse(getResultObjects2.stream().anyMatch(p -> p.getId().equals(resultObject.getId())));
  }
}
