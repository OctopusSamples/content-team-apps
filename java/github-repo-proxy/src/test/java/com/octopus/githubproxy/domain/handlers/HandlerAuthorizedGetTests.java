package com.octopus.githubproxy.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.octopus.exceptions.UnauthorizedException;
import com.octopus.features.DisableSecurityFeature;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.List;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerAuthorizedGetTests {

  @Inject
  ResourceHandler handler;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @Test
  @Transactional
  public void testGetResource() {
    assertThrows(UnauthorizedException.class, () -> handler.getOne(
        "1",
        List.of("main"),
        null,
        null,
        ""));
  }
}
