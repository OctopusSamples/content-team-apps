package com.octopus.githubrepo.domain.handlers.createcommit;

import static org.mockito.ArgumentMatchers.any;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.encryption.AsymmetricDecryptor;
import com.octopus.encryption.CryptoUtils;
import com.octopus.exceptions.ServerErrorException;
import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.handlers.GitHubCommitHandler;
import com.octopus.githubrepo.infrastructure.clients.GitHubClient;
import com.octopus.githubrepo.infrastructure.clients.PopulateRepoClient;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import java.io.IOException;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * Simulates a failed request to find the existing repo.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class HandlerRepoApiCallFailedTests extends BaseGitHubTest {

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

  @BeforeAll
  public void setup() throws IOException {
    mockGitHubClient(gitHubClient);

    // Mock an error response when querying the repo
    final Response errorResponse = Mockito.mock(Response.class);
    Mockito.when(errorResponse.getStatus()).thenReturn(500);
    final ClientWebApplicationException errorException = Mockito.mock(
        ClientWebApplicationException.class);
    Mockito.when(errorException.getResponse()).thenReturn(errorResponse);
    Mockito.when(gitHubClient.getRepo(any(), any(), any())).thenThrow(errorException);

    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(false);
    Mockito.when(jwtUtils.getJwtFromAuthorizationHeader(any())).thenReturn(Optional.of(""));
    Mockito.when(jwtInspector.jwtContainsScope(any(), any(), any())).thenReturn(true);
    Mockito.when(cognitoAdminClaim.getAdminClaim()).thenReturn(Optional.of("admin-claim"));
    Mockito.when(cryptoUtils.decrypt(any(), any(), any())).thenReturn("decrypted");
    Mockito.when(asymmetricDecryptor.decrypt(any(), any())).thenReturn("decrypted");

    final Response acceptedResponse = Mockito.mock(Response.class);
    Mockito.when(acceptedResponse.getStatus()).thenReturn(202);
    Mockito.when(populateRepoClient.populateRepo(any(), any(), any(), any(), any(), any())).thenReturn(acceptedResponse);
  }

  @Test
  @Transactional
  public void testCreateResource() throws DocumentSerializationException {
    Assertions.assertThrows(
        ServerErrorException.class, () ->  createResource(handler, resourceConverter));
  }
}
