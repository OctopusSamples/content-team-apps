package com.octopus.octopusproxy.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.encryption.AsymmetricDecryptor;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.exceptions.InvalidFilterException;
import com.octopus.exceptions.JsonSerializationException;
import com.octopus.exceptions.ServerErrorException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.octopusproxy.BaseTest;
import com.octopus.octopusproxy.domain.features.ClientPrivateKey;
import com.octopus.octopusproxy.domain.framework.WireMockExtensions;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

/**
 * These tests are mostly focused on the retrieval of new resources through GET operations.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(WireMockExtensions.class)
public class HandlerGetAllTests extends BaseTest {

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
  public void getAllResourceTestNull() {
    assertThrows(NullPointerException.class, () -> {
      handler.getAll(
          null,
          "name==EKS;instance==https://main.testoctopus.app",
          List.of("testing"),
          null,
          null);
    });

    assertThrows(NullPointerException.class, () -> {
      handler.getAll(
          "apikey",
          "name==EKS;instance==https://main.testoctopus.app",
          null,
          null,
          null);
    });
  }

  @Test
  public void testBadFilter()  {
    assertThrows(InvalidFilterException.class, () ->  handler.getAll(
        "apikey",
        "name=='ECS mcasperson'",
        List.of("testing"),
        null,
        null));

    assertThrows(InvalidFilterException.class, () ->  handler.getAll(
        "apikey",
        "instance==" + wiremockUrl,
        List.of("testing"),
        null,
        null));

    assertThrows(InvalidFilterException.class, () ->  handler.getAll(
        "apikey",
        "notarecognisedfilter=='ECS mcasperson';instance==" + wiremockUrl,
        List.of("testing"),
        null,
        null));
  }

  @Test
  public void testInvalidInstance()  {
    assertThrows(EntityNotFoundException.class, () ->  handler.getAll(
        "apikey",
        "name=='ECS mcasperson';instance==thisisnotaurl",
        List.of("testing"),
        null,
        null));
  }

  @Test
  public void testUnauthorizedUpstream()  {
    assertThrows(UnauthorizedException.class, () ->  handler.getAll(
        "apikey",
        "name=='unauthorized';instance==" + wiremockUrl,
        List.of("testing"),
        null,
        null));
  }

  @Test
  public void testMissingUpstream()  {
    assertThrows(EntityNotFoundException.class, () ->  handler.getAll(
        "apikey",
        "name=='missing';instance==" + wiremockUrl,
        List.of("testing"),
        null,
        null));
  }

  @Test
  public void testBadUpstream()  {
    assertThrows(ServerErrorException.class, () ->  handler.getAll(
        "apikey",
        "name=='bad';instance==" + wiremockUrl,
        List.of("testing"),
        null,
        null));
  }

  @Test
  public void testCorruptUpstream()  {
    assertThrows(JsonSerializationException.class, () ->  handler.getAll(
        "apikey",
        "name=='corrupt';instance==" + wiremockUrl,
        List.of("testing"),
        null,
        null));
  }

  @ParameterizedTest
  @ValueSource(strings = {"ECS mcasperson", "ECS octo-matt"})
  public void getAllSpaces(final String space) throws DocumentSerializationException, JsonProcessingException {
    final String result = handler.getAll(
        "apikey",
        "name=='" + space + "';instance==" + wiremockUrl,
        List.of("testing"),
        null,
        null);

    assertTrue(((List)MAPPER.readValue(result, Map.class).get("data")).size() != 0);
  }
}
