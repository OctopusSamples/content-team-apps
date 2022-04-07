package com.octopus.githubrepo.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.encryption.CryptoUtils;
import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.githubrepo.BaseTest;
import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.entities.CreateGithubRepo;
import com.octopus.githubrepo.domain.entities.github.GitHubCommit;
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
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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

/**
 * Simulate tests the population of a repo when the repo already exists.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class HandlerExistingRepoAuthorizedWithMockedServiceTokenTests extends BaseGitHubTest  {

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
  GitHubRepoHandler handler;

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
    super.mockGithubClient(gitHubBuilder);

    final Response notFoundResponse = Mockito.mock(Response.class);
    Mockito.when(notFoundResponse.getStatus()).thenReturn(404);

    final Response mockScopeResponse = Mockito.mock(Response.class);
    Mockito.when(mockScopeResponse.getHeaderString("X-OAuth-Scopes")).thenReturn("workflow,repo");

    final Response mockRepoResponse = Mockito.mock(Response.class);
    Mockito.when(mockRepoResponse.getStatus()).thenReturn(200);

    final ClientWebApplicationException getBranchException = Mockito.mock(
        ClientWebApplicationException.class);
    Mockito.when(getBranchException.getResponse()).thenReturn(notFoundResponse);

    final Response linksResponse = Mockito.mock(Response.class);
    Mockito.when(linksResponse.getHeaderString(any()))
        .thenReturn("Link: <https://api.github.com/repos?page=3&per_page=100>; rel=\"next\",\n"
            + "<https://api.github.com/repos?page=50&per_page=100>; rel=\"last\"");

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
    Mockito.when(gitHubClient.getCommitsRaw(any(), any(), anyInt(), any()))
        .thenReturn(linksResponse);
    Mockito.when(gitHubClient.getCommits(any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(List.of(
            GitHubCommit.builder().sha("sha12345").build()));
    Mockito.when(gitHubClient.getBranch(any(), any(), any(), any())).thenThrow(getBranchException);
    Mockito.when(gitHubClient.getRepo(any(), any(), any())).thenReturn(mockRepoResponse);
    Mockito.when(gitHubClient.getUser(any()))
        .thenReturn(GitHubUser.builder().login("testuser").build());
    Mockito.when(gitHubClient.getPublicKey(any(), any(), any()))
        .thenReturn(GitHubPublicKey.builder().key("test").keyId("test").build());
    Mockito.when(generateTemplateClient.generateTemplate(any(), any(), any(), any()))
        .thenReturn(zipFileResponse);

  }

  @Test
  @Transactional
  public void testCreateResource() throws DocumentSerializationException {
    final CreateGithubRepo resource = createResource(handler, resourceConverter);
    assertEquals("myrepo", resource.getGithubRepository());
  }
}
