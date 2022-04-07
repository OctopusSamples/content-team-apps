package com.octopus.githubrepo.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.encryption.CryptoUtils;
import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.githubrepo.BaseTest;
import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.entities.CreateGithubRepo;

import com.octopus.githubrepo.domain.entities.github.GitHubPublicKey;
import com.octopus.githubrepo.domain.entities.github.GitHubUser;
import com.octopus.githubrepo.infrastructure.clients.GenerateTemplateClient;
import com.octopus.githubrepo.infrastructure.clients.GitHubClient;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommitBuilder;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeBuilder;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.connector.GitHubConnector;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class HandlerTests extends BaseTest {

  private static final String HEALTH_ENDPOINT = "/health/serviceaccounts";

  @Inject
  GitHubRepoHandler handler;

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

  @Inject
  ResourceConverter resourceConverter;

  @InjectMock
  GitHubBuilder gitHubBuilder;

  @RestClient
  @InjectMock
  GitHubClient gitHubClient;

  @RestClient
  @InjectMock
  GenerateTemplateClient generateTemplateClient;

  @BeforeAll
  public void setup() throws IOException {
    final Response mockScopeResponse = Mockito.mock(Response.class);
    Mockito.when(mockScopeResponse.getHeaderString("X-OAuth-Scopes")).thenReturn("workflow,repo");

    final Response mockRepoResponse = Mockito.mock(Response.class);
    Mockito.when(mockRepoResponse.getStatus()).thenReturn(404);

    final Response zipFileResponse = Mockito.mock(Response.class);
    Mockito.when(zipFileResponse.getStatus()).thenReturn(200);
    // Return the smallest legal ZIP file possible
    Mockito.when(zipFileResponse.readEntity(InputStream.class)).thenReturn(new ByteArrayInputStream(
        new byte[]{80, 75, 05, 06, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00,
            00, 00}));

    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(false);
    Mockito.when(jwtUtils.getJwtFromAuthorizationHeader(any())).thenReturn(Optional.of(""));
    Mockito.when(jwtInspector.jwtContainsScope(any(), any(), any())).thenReturn(true);
    Mockito.when(cognitoAdminClaim.getAdminClaim()).thenReturn(Optional.of("admin-claim"));
    Mockito.when(cryptoUtils.decrypt(any(), any(), any())).thenReturn("decrypted");
    Mockito.when(gitHubClient.checkRateLimit(any())).thenReturn(mockScopeResponse);
    Mockito.when(gitHubClient.getRepo(any(), any(), any())).thenReturn(mockRepoResponse);
    Mockito.when(gitHubClient.getUser(any()))
        .thenReturn(GitHubUser.builder().login("testuser").build());
    Mockito.when(gitHubClient.getPublicKey(any(), any(), any()))
        .thenReturn(GitHubPublicKey.builder().key("test").keyId("test").build());
    Mockito.when(generateTemplateClient.generateTemplate(any(), any(), any(), any()))
        .thenReturn(zipFileResponse);

    // We need to stub out all interactions with GitHub via the third party github client

    Mockito.when(gitHubBuilder.withOAuthToken(any())).thenReturn(gitHubBuilder);
    Mockito.when(gitHubBuilder.withConnector(ArgumentMatchers.<GitHubConnector>any()))
        .thenReturn(gitHubBuilder);

    final GitHub gitHub = Mockito.mock(GitHub.class);
    final GHRepository repo = Mockito.mock(GHRepository.class);
    final GHTreeBuilder treeBuilder = Mockito.mock(GHTreeBuilder.class);
    final GHCommitBuilder commitBuilder = Mockito.mock(GHCommitBuilder.class);
    final GHCommit commit = Mockito.mock(GHCommit.class);
    final GHRef ref = Mockito.mock(GHRef.class);

    Mockito.doNothing().when(ref).updateTo(any());
    Mockito.when(commitBuilder.tree(any())).thenReturn(commitBuilder);
    Mockito.when(commitBuilder.parent(any())).thenReturn(commitBuilder);
    Mockito.when(commitBuilder.message(any())).thenReturn(commitBuilder);
    Mockito.when(commitBuilder.create()).thenReturn(commit);
    Mockito.when(treeBuilder.baseTree(any())).thenReturn(treeBuilder);
    Mockito.when(treeBuilder.create()).thenReturn(Mockito.mock(GHTree.class));
    Mockito.when(treeBuilder.add(anyString(), any(byte[].class), anyBoolean()))
        .thenReturn(treeBuilder);
    Mockito.when(repo.createTree()).thenReturn(treeBuilder);
    Mockito.when(repo.createCommit()).thenReturn(commitBuilder);
    Mockito.when(repo.getRef(any())).thenReturn(ref);
    Mockito.when(repo.getBranch(any())).thenReturn(Mockito.mock(GHBranch.class));
    Mockito.when(gitHub.getRepository(any())).thenReturn(repo);

    Mockito.when(gitHubBuilder.build()).thenReturn(gitHub);
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
  @Transactional
  public void createResourceTestNull() {
    assertThrows(NullPointerException.class, () -> {
      handler.create(
          null,
          null,
          null,
          null,
          null);
    });

    assertThrows(NullPointerException.class, () -> {
      final CreateGithubRepo audit = createResource();
      handler.create(resourceToResourceDocument(resourceConverter, audit),
          null,
          null,
          null,
          null);
    });
  }

  @Test
  @Transactional
  public void testCreateResource() throws DocumentSerializationException {
    final CreateGithubRepo resultObject = createResource(handler, resourceConverter);
    assertEquals("myrepo", resultObject.getGithubRepository());
  }
}
