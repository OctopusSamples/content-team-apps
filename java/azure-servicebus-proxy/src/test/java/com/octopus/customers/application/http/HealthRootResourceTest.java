package com.octopus.customers.application.http;

import static io.restassured.RestAssured.given;

import com.octopus.customers.BaseTest;
import com.octopus.customers.application.Paths;
import io.quarkus.test.junit.QuarkusTest;
import lombok.NonNull;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HealthRootResourceTest extends BaseTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
          Paths.HEALTH_ENDPOINT + "/POST",
      })
  public void testCreateAndGetResource(@NonNull final String path) {
    given().when().get(path).then().statusCode(200);
  }
}
