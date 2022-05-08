package com.octopus.octopusproxy.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.octopusproxy.BaseTest;
import com.octopus.octopusproxy.domain.features.ClientPrivateKey;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerAuthorizedGetTests extends BaseTest {

  @Inject
  ResourceHandler handler;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @InjectMock
  ClientPrivateKey clientPrivateKey;

  @BeforeEach
  public void setup() {
    Mockito.when(clientPrivateKey.privateKeyBase64()).thenReturn(Optional.of(""));
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(false);
  }

  @Test
  @Transactional
  public void testGetResource() {
    assertThrows(UnauthorizedException.class, () -> handler.getOne(
        "1",
        "blah",
        List.of("main"),
        null, null));
  }

}
