package com.octopus.octopusproxy.application.http;

import static io.restassured.RestAssured.given;

import com.octopus.octopusproxy.BaseTest;
import com.octopus.octopusproxy.application.Paths;
import io.quarkus.test.junit.QuarkusTest;
import lombok.NonNull;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * These tests verify the health endpoints.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HealthRootResourceGetOneTest extends BaseTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
          Paths.HEALTH_ENDPOINT + "/x/GET"
      })
  public void testCreateAndGetResource(@NonNull final String path) {
    given().when().get(path).then().statusCode(200);
  }
}
