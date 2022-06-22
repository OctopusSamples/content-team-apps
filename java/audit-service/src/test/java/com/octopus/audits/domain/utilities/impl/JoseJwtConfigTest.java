package com.octopus.audits.domain.utilities.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.audits.domain.features.impl.DisableSecurityFeature;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.sql.SQLException;
import javax.inject.Inject;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JoseJwtConfigTest {

  @Inject
  JoseJwtVerifier joseJwtVerifier;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @BeforeAll
  public void setup() throws SQLException, LiquibaseException {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(true);
  }

  @Test
  public void verifyClaimsExtraction() {
    assertTrue(joseJwtVerifier.configIsValid());
  }
}
