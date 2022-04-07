package com.octopus.githubrepo.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.anyString;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.google.common.collect.ImmutableMultimap;
import com.octopus.encryption.CryptoUtils;
import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.entities.CreateGithubRepo;
import com.octopus.githubrepo.domain.entities.github.GitHubPublicKey;
import com.octopus.githubrepo.domain.entities.github.GitHubUser;
import com.octopus.githubrepo.infrastructure.clients.GenerateTemplateClient;
import com.octopus.githubrepo.infrastructure.clients.GitHubClient;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import com.octopus.githubrepo.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.common.util.CaseInsensitiveMap;
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
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Simulate tests when a machine-to-machine token has been passed in.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class HandlerAuthorizedWithMockedServiceTokenTests extends BaseTest {

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

  @Test
  @Transactional
  public void testCreateResource() throws DocumentSerializationException {
    final CreateGithubRepo resource = createResource(handler, resourceConverter);
    assertEquals("myrepo", resource.getGithubRepository());
  }
}
