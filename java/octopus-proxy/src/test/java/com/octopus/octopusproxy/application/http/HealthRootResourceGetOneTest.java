package com.octopus.octopusproxy.application.http;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.octopusproxy.BaseTest;
import com.octopus.octopusproxy.application.Paths;
import com.octopus.octopusproxy.domain.features.ClientPrivateKey;
import com.octopus.octopusproxy.domain.handlers.HealthHandler;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.Optional;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

/**
 * These tests verify the health endpoints.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HealthRootResourceGetOneTest extends BaseTest {

  @InjectMock
  ClientPrivateKey clientPrivateKey;

  @BeforeEach
  public void setup() {
    Mockito.when(clientPrivateKey.privateKeyBase64()).thenReturn(Optional.of(""));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
          Paths.HEALTH_ENDPOINT + "/x/GET"
      })
  public void testCreateAndGetResource(@NonNull final String path) {
    given().when().get(path).then().statusCode(200);
  }
}
