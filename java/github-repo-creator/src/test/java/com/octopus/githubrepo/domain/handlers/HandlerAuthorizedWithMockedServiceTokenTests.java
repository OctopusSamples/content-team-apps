package com.octopus.githubrepo.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.anyString;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.io.Resources;
import com.octopus.encryption.AsymmetricDecryptor;
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
import org.mockito.invocation.InvocationOnMock;

/**
 * Simulate tests when a machine-to-machine token has been passed in.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class HandlerAuthorizedWithMockedServiceTokenTests extends BaseGitHubTest {

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
    mockGithubClient(gitHubBuilder);
    mockGithubClient(gitHubClient, false, true);

    final Response zipFileResponse = Mockito.mock(Response.class);
    Mockito.when(zipFileResponse.getStatus()).thenReturn(200);
    Mockito.when(zipFileResponse.readEntity(InputStream.class))
        .thenAnswer((InvocationOnMock invocation) -> new ByteArrayInputStream(
            Resources.toByteArray(Resources.getResource("template.zip"))));

    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(false);
    Mockito.when(jwtUtils.getJwtFromAuthorizationHeader(any())).thenReturn(Optional.of(""));
    Mockito.when(jwtInspector.jwtContainsScope(any(), any(), any())).thenReturn(true);
    Mockito.when(cognitoAdminClaim.getAdminClaim()).thenReturn(Optional.of("admin-claim"));
    Mockito.when(cryptoUtils.decrypt(any(), any(), any())).thenReturn("decrypted");
    Mockito.when(asymmetricDecryptor.decrypt(any(), any())).thenReturn("decrypted");
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
