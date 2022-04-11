package com.octopus.githubrepo.application.http;

import static io.restassured.RestAssured.given;

import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.handlers.populaterepo.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import lombok.NonNull;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class HttpHealthTest extends BaseTest {

  private static final String HEALTH_ENDPOINT = "/health/populategithubrepo";

  @ParameterizedTest
  @ValueSource(
      strings = {
          HEALTH_ENDPOINT + "/POST",
      })
  public void testCreateAndGetAudit(@NonNull final String path) {
    given().when().get(path).then().statusCode(200);
  }
}
