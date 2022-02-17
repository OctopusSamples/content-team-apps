package com.octopus.audits.application.http;

import static io.restassured.RestAssured.given;

import com.octopus.audits.BaseTest;
import com.octopus.audits.infrastructure.utilities.LiquidbaseUpdater;
import io.quarkus.test.junit.QuarkusTest;
import java.sql.SQLException;
import javax.inject.Inject;
import liquibase.exception.LiquibaseException;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpHealthTest extends BaseTest {

  @Inject LiquidbaseUpdater liquidbaseUpdater;

  @BeforeAll
  public void setup() throws SQLException, LiquibaseException {
    liquidbaseUpdater.update();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/health/audits/GET",
        "/health/audits/POST",
        "/health/audits/x/GET"
      })
  public void testCreateAndGetAudit(@NonNull final String path) {
    given().when().get(path).then().statusCode(200);
  }
}
