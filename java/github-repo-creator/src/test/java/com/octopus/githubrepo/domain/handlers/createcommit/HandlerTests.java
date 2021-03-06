package com.octopus.githubrepo.domain.handlers.createcommit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.encryption.AsymmetricDecryptor;
import com.octopus.encryption.CryptoUtils;
import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.audit.AuditGenerator;
import com.octopus.githubrepo.domain.entities.CreateGithubCommit;
import com.octopus.githubrepo.domain.handlers.GitHubCommitHandler;
import com.octopus.githubrepo.domain.handlers.HealthHandler;
import com.octopus.githubrepo.infrastructure.clients.GitHubClient;
import com.octopus.githubrepo.infrastructure.clients.PopulateRepoClient;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(Lifecycle.PER_METHOD)
@TestProfile(TestingProfile.class)
public class HandlerTests extends BaseGitHubTest {

  private static final String HEALTH_ENDPOINT = "/health/githubcommit";

  @Inject
  GitHubCommitHandler handler;

  @Inject
  HealthHandler healthHandler;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @InjectMock
  AdminJwtClaimFeature cognitoAdminClaim;

  @InjectMock
  JwtInspector jwtInspector;

  @InjectMock
  JwtUtils jwtUtils;

  @InjectMock
  CryptoUtils cryptoUtils;

  @InjectMock
  AsymmetricDecryptor asymmetricDecryptor;

  @Inject
  ResourceConverter resourceConverter;

  @RestClient
  @InjectMock
  GitHubClient gitHubClient;

  @RestClient
  @InjectMock
  PopulateRepoClient populateRepoClient;

  @InjectMock
  AuditGenerator auditGenerator;

  @BeforeEach
  public void setup() throws IOException {
    mockGitHubClient(gitHubClient);

    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(false);
    Mockito.when(jwtUtils.getJwtFromAuthorizationHeader(any())).thenReturn(Optional.of(""));
    Mockito.when(jwtInspector.jwtContainsScope(any(), any(), any())).thenReturn(true);
    Mockito.when(cognitoAdminClaim.getAdminClaim()).thenReturn(Optional.of("admin-claim"));
    Mockito.when(cryptoUtils.decrypt(any(), any(), any())).thenReturn("decrypted");
    Mockito.when(asymmetricDecryptor.decrypt(any(), any())).thenReturn("decrypted");

    final Response acceptedResponse = Mockito.mock(Response.class);
    Mockito.when(acceptedResponse.getStatus()).thenReturn(202);
    final CompletableFuture<Response> eventualResponse = new CompletableFuture<>();
    eventualResponse.complete(acceptedResponse);

    Mockito.when(populateRepoClient.populateRepo(any(), any(), any(), any(), any(), any())).thenReturn(eventualResponse);
  }

  @ParameterizedTest
  @CsvSource({
      HEALTH_ENDPOINT + ",POST",
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
  public void createResourceTestNull() {
    assertThrows(NullPointerException.class, () -> {
      handler.create(
          null,
          null,
          null,
          null,
          null,
          null,
          "blah");
    });

    assertThrows(NullPointerException.class, () -> {
      final CreateGithubCommit resource = createResource();
      handler.create(resourceToResourceDocument(resourceConverter, resource),
          null,
          null,
          null,
          null,
          null,
          null);
    });
  }

  @Test
  public void testCreateResource() throws DocumentSerializationException {
    final CreateGithubCommit resultObject = createResource(handler, resourceConverter);
    assertEquals("myrepo", resultObject.getGithubRepository());
  }

  @Test
  public void testCreateResourceWithNewRepo() throws DocumentSerializationException {
    final CreateGithubCommit resultObject = createResource(handler, resourceConverter, true, true);
    assertTrue(resultObject.getGithubRepository().matches("myrepo\\d+"));
  }
}
