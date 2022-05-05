package com.octopus.octopusproxy.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.octopus.exceptions.UnauthorizedException;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.octopusproxy.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.List;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
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

  @BeforeAll
  public void setup() {
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
