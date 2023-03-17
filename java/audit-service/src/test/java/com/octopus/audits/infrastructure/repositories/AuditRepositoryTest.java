package com.octopus.audits.infrastructure.repositories;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.octopus.audits.infrastructure.utilities.LiquidbaseUpdater;
import io.quarkus.test.junit.QuarkusTest;
import java.sql.SQLException;
import javax.inject.Inject;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
public class AuditRepositoryTest {
  @Inject
  AuditRepository auditRepository;

  @Inject
  LiquidbaseUpdater liquidbaseUpdater;

  @BeforeAll
  public void setup() throws SQLException, LiquibaseException {
    liquidbaseUpdater.update(true);
  }

  @Test
  public void verifyFindNone() {
    assertNull(auditRepository.findOne(10000000));
  }

  @Test
  public void verifyNullInputs() {
    assertThrows(NullPointerException.class, () -> {
      auditRepository.findAll(null, null, null, null);
    });

    assertThrows(NullPointerException.class, () -> {
      auditRepository.save(null);
    });
  }
}
