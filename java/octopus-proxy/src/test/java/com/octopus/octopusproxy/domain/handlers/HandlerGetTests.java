package com.octopus.octopusproxy.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.encryption.AsymmetricDecryptor;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.octopusproxy.BaseTest;
import com.octopus.octopusproxy.domain.features.ClientPrivateKey;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * These tests are mostly focused on the retrieval of new resources through GET operations.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerGetTests extends BaseTest {

  @Inject
  ResourceHandler handler;

  @InjectMock
  ClientPrivateKey clientPrivateKey;

  @InjectMock
  AsymmetricDecryptor asymmetricDecryptor;

  @BeforeEach
  public void setup() {
    Mockito.when(asymmetricDecryptor.decrypt(any(), any())).thenReturn("");
    Mockito.when(clientPrivateKey.privateKeyBase64()).thenReturn(Optional.of(""));
  }

  @Test
  @Transactional
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
  @Transactional
  public void getMissingResource() {
    assertThrows(EntityNotFoundException.class, () ->
      handler.getOne(
          "1000000000000000000",
          "blah",
          List.of("main"),
          null, null)
    );
  }
}
