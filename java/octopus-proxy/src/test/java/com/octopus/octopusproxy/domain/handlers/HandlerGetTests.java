package com.octopus.octopusproxy.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.octopusproxy.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * These tests are mostly focused on the retrieval of new resources through GET operations.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerGetTests extends BaseTest {

  @Inject
  ResourceHandler handler;

  @Inject
  HealthHandler healthHandler;

  @Inject
  ResourceConverter resourceConverter;

  @Test
  @Transactional
  public void getOneResourceTestNull() {
    assertThrows(NullPointerException.class, () -> {
      handler.getOne(
          null,
          "blah",
          List.of("testing"),
          null,
          null);
    });

    assertThrows(NullPointerException.class, () -> {
      handler.getOne(
          "1",
          null,
          List.of("testing"),
          null,
          null);
    });

    assertThrows(NullPointerException.class, () -> {
      handler.getOne(
          "1",
          "blah",
          null,
          null,
          null);
    });
  }

  @Test
  @Transactional
  public void getMissingResource() {
    assertThrows(EntityNotFoundException.class, () ->
      handler.getOne(
          "1000000000000000000",
          "blah",
          List.of("main"),
          null, null)
    );
  }
}
