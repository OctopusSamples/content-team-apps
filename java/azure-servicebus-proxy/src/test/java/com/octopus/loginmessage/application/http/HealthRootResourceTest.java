package com.octopus.loginmessage.application.http;

import static io.restassured.RestAssured.given;

import com.octopus.loginmessage.BaseTest;
import com.octopus.loginmessage.CommercialAzureServiceBusTestProfile;
import com.octopus.loginmessage.application.TestPaths;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import lombok.NonNull;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(CommercialAzureServiceBusTestProfile.class)
public class HealthRootResourceTest extends BaseTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
          TestPaths.HEALTH_ENDPOINT + "/POST",
      })
  public void testCreateAndGetResource(@NonNull final String path) {
    given().when().get(path).then().statusCode(200);
  }
}
