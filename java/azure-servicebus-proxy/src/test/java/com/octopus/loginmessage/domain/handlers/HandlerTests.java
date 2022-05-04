package com.octopus.loginmessage.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.loginmessage.BaseTest;
import com.octopus.loginmessage.CommercialAzureServiceBusTestProfile;
import com.octopus.loginmessage.application.TestPaths;
import com.octopus.loginmessage.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.loginmessage.infrastructure.octofront.CommercialServiceBus;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

/**
 * Tests that validate the messages being sent to the upstream service bus and of the health checks.
 * Authorization is disabled.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(CommercialAzureServiceBusTestProfile.class)
public class HandlerTests extends BaseTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String XRAY = "Self=1-62382f80-7bb0215908d69e92762eda43;Root=1-62382f7f-11fd455f1b8a116b3047ae73";

  private static final String XRAY_SELF = "1-62382f80-7bb0215908d69e92762eda43";

  private static final List<String> ALLOWED_KEYS = List.of(
      "UtmParameters",
      "EmailAddress",
      "ProgrammingLanguage",
      "GitHubUsername",
      "FirstName",
      "LastName",
      "ToolName");

  @Inject
  ResourceHandler handler;

  @Inject
  HealthHandler healthHandler;

  @Inject
  ResourceConverter resourceConverter;

  @InjectMock
  CommercialServiceBus commercialServiceBus;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @BeforeAll
  public void setup() {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(true);
    Mockito.doAnswer(invocation  -> {
      final String traceId = invocation.getArgument(0, String.class);
      final String body = invocation.getArgument(1, String.class);

      assertEquals(XRAY_SELF, traceId);

      final Map<String, Object> bodyObject = OBJECT_MAPPER.readValue(body, Map.class);

      // We must use PascalCase, so the first character must be uppercase
      bodyObject.keySet().forEach(k -> assertEquals(
          k.substring(0, 1).toUpperCase(Locale.ROOT),
          k.substring(0, 1)));
      // The body must only contain known keys
      bodyObject.keySet().forEach(k -> assertTrue(ALLOWED_KEYS.contains(k)));

      return null;
    }).when(commercialServiceBus).sendUserDetails(any(), any());
  }

  @ParameterizedTest
  @CsvSource({
      TestPaths.HEALTH_ENDPOINT + ",POST",
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

  @Test
  @Transactional
  public void createResourceTestNull() {
    assertThrows(NullPointerException.class, () -> {
      handler.create(
          null,
          List.of("testing"),
          null,
          null,
          null);
    });

    assertThrows(NullPointerException.class, () -> {
      final GithubUserLoggedInForFreeToolsEventV1 resource = createResource("a@a.com");
      handler.create(resourceToResourceDocument(resourceConverter, resource),
          null,
          null,
          null,
          null);
    });
  }

  @Test
  @Transactional
  public void testCreateResource() {
    assertDoesNotThrow(() -> handler.create(
        resourceToResourceDocument(resourceConverter, new GithubUserLoggedInForFreeToolsEventV1()),
        List.of("main"),
        null, null,
        XRAY));
  }
}
