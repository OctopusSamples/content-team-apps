package com.octopus.products.infrastructure.repositories;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.octopus.products.infrastructure.utilities.LiquidbaseUpdater;
import io.quarkus.test.junit.QuarkusTest;
import java.sql.SQLException;
import javax.inject.Inject;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

/**
 * These tests verify the database access.
 */
@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
public class RepositoryTest {
  @Inject
  ProductsRepository repository;

  @Inject
  LiquidbaseUpdater liquidbaseUpdater;

  @BeforeAll
  public void setup() throws SQLException, LiquibaseException {
    liquidbaseUpdater.update();
  }

  @Test
  public void verifyFindNone() {
    assertNull(repository.findOne(10000000));
  }

  @Test
  public void verifyNullInputs() {
    assertThrows(NullPointerException.class, () -> {
      repository.findAll(null, null, null, null);
    });

    assertThrows(NullPointerException.class, () -> {
      repository.save(null);
    });
  }
}
