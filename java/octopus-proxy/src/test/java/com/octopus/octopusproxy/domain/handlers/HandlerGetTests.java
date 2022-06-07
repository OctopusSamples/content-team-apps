package com.octopus.octopusproxy.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.encryption.AsymmetricDecryptor;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.octopusproxy.BaseTest;
import com.octopus.octopusproxy.domain.features.ClientPrivateKey;
import com.octopus.octopusproxy.domain.framework.WireMockExtensions;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * These tests are mostly focused on the retrieval of new resources through GET operations.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(WireMockExtensions.class)
public class HandlerGetTests extends BaseTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Inject
  ResourceHandler handler;

  @InjectMock
  ClientPrivateKey clientPrivateKey;

  @InjectMock
  AsymmetricDecryptor asymmetricDecryptor;

  @ConfigProperty(name = "wiremock.url")
  String wiremockUrl;

  @BeforeEach
  public void setup() {
    Mockito.when(asymmetricDecryptor.decrypt(any(), any())).thenReturn("");
    Mockito.when(clientPrivateKey.privateKeyBase64()).thenReturn(Optional.of(""));
  }

  @Test
  public void getOneResourceTestNull() {
    assertThrows(NullPointerException.class, () -> {
      handler.getOne(
          null,
          "blah",
          List.of("testing"),
          null,
          null);
    });

    assertThrows(NullPointerException.class, () -> {
      handler.getOne(
          "1",
          null,
          List.of("testing"),
          null,
          null);
    });

    assertThrows(NullPointerException.class, () -> {
      handler.getOne(
          "1",
          "blah",
          null,
          null,
          null);
    });
  }

  @Test
  public void getMissingResource() {
    assertThrows(EntityNotFoundException.class, () ->
      handler.getOne(
          "1000000000000000000",
          "blah",
          List.of("main"),
          null,
          null)
    );
  }

  @Test
  public void getSpace() throws DocumentSerializationException, JsonProcessingException {
    final String result = handler.getOne(
            wiremockUrl + "/api/Spaces/Spaces-1",
            "blah",
            List.of("main"),
            null,
        null);

    assertTrue(StringUtils.isNotBlank(((Map) MAPPER.readValue(result, Map.class).get("data")).get("id").toString()));
  }

  @Test
  public void getMissingSpace() {
    assertThrows(EntityNotFoundException.class, () -> handler.getOne(
        wiremockUrl + "/api/Spaces/Spaces-2",
        "blah",
        List.of("main"),
        null,
        null));
  }
}
