package com.octopus.serviceaccount.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.serviceaccount.BaseTest;
import com.octopus.serviceaccount.domain.entities.ServiceAccount;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerTests extends BaseTest {

  private static final String HEALTH_ENDPOINT = "/health/serviceaccounts";

  @Inject
  ServiceAccountHandler handler;

  @Inject
  HealthHandler healthHandler;

  @Inject
  ResourceConverter resourceConverter;

  @ParameterizedTest
  @CsvSource({
      HEALTH_ENDPOINT + ",POST",
  })
  public void testHealth(@NonNull final String path, @NonNull final String method)
      throws DocumentSerializationException {
    assertNotNull(healthHandler.getHealth(path, method));
  }

  @Test
  public void testHealthNulls() {
    assertThrows(NullPointerException.class, () -> healthHandler.getHealth(null, "GET"));
    assertThrows(NullPointerException.class, () -> healthHandler.getHealth("blah", null));
  }

  @Test
  @Transactional
  public void createResourceTestNull() {
    assertThrows(NullPointerException.class, () -> {
      handler.create(
          null,
          null,
          null);
    });

    assertThrows(NullPointerException.class, () -> {
      final ServiceAccount audit = createResource("subject");
      handler.create(resourceToResourceDocument(resourceConverter, audit),
          null,
          null);
    });
  }

  @Test
  @Transactional
  public void testCreateResource() throws DocumentSerializationException {
    final ServiceAccount resultObject = createResource(handler, resourceConverter);
    assertNotNull(resultObject.getId());
    assertEquals("myname", resultObject.getName());
    assertEquals("A description", resultObject.getDescription());
  }
}
