package com.octopus.githubrepo.domain.handlers.createcommit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * Simulates a situation where the repo does not already exist.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class HandlerRepoNotExistsTests extends BaseGitHubTest {

  @Inject
  GitHubCommitHandler handler;

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

  @BeforeAll
  public void setup() throws IOException {
    mockGitHubClient(gitHubClient);

    // Mock a not found response when querying the repo
    final Response notFoundResponse = Mockito.mock(Response.class);
    Mockito.when(notFoundResponse.getStatus()).thenReturn(404);
    Mockito.when(gitHubClient.getRepo(any(), any(), any())).thenReturn(notFoundResponse);

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

  @Test
  @Transactional
  public void testCreateResource() throws DocumentSerializationException {
    final CreateGithubCommit resultObject = createResource(handler, resourceConverter);
    assertEquals("myrepo", resultObject.getGithubRepository());
  }
}
