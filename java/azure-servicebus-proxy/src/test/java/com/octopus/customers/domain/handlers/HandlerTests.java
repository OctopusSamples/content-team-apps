package com.octopus.customers.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.customers.BaseTest;
import com.octopus.customers.application.Paths;
import com.octopus.customers.domain.entities.Customer;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
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

  @Inject
  ResourceHandler handler;

  @Inject
  HealthHandler healthHandler;

  @Inject
  ResourceConverter resourceConverter;


  @ParameterizedTest
  @CsvSource({
      Paths.HEALTH_ENDPOINT + ",POST",
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
}
