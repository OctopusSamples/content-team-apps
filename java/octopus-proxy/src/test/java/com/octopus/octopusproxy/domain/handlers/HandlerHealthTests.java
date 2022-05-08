package com.octopus.octopusproxy.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.octopusproxy.BaseTest;
import com.octopus.octopusproxy.application.Paths;
import com.octopus.octopusproxy.domain.features.ClientPrivateKey;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.Optional;
import javax.inject.Inject;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

/**
 * These tests are focused on the health endpoints.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerHealthTests extends BaseTest {

  @Inject
  HealthHandler healthHandler;

  @InjectMock
  ClientPrivateKey clientPrivateKey;

  @BeforeEach
  public void setup() {
    Mockito.when(clientPrivateKey.privateKeyBase64()).thenReturn(Optional.of(""));
  }

  @ParameterizedTest
  @CsvSource({
      Paths.HEALTH_ENDPOINT + "/x,GET"
  })
  public void testHealth(@NonNull final String path, @NonNull final String method)
      throws DocumentSerializationException {
    assertNotNull(healthHandler.getHealth(path, method));
  }

  @Test
  public void testHealthNulls() {
    assertThrows(NullPointerException.class, () -> healthHandler.getHealth(null, "GET"));
    assertThrows(NullPointerException.class, () -> healthHandler.getHealth("blah", null));
  }
}
