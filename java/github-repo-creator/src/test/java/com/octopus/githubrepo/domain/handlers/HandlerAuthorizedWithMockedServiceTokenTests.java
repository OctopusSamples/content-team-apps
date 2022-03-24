package com.octopus.githubrepo.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.githubrepo.domain.entities.CreateGithubRepo;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import com.octopus.githubrepo.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * Simulate tests when a machine-to-machine token has been passed in.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerAuthorizedWithMockedServiceTokenTests extends BaseTest {

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @InjectMock
  AdminJwtClaimFeature cognitoAdminClaim;

  @InjectMock
  JwtInspector jwtInspector;

  @InjectMock
  JwtUtils jwtUtils;

  @Inject
  GitHubRepoHandler handler;

  @Inject
  ResourceConverter resourceConverter;

  @BeforeAll
  public void setup() {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(false);
    Mockito.when(jwtUtils.getJwtFromAuthorizationHeader(any())).thenReturn(Optional.of(""));
    Mockito.when(jwtInspector.jwtContainsScope(any(), any(), any())).thenReturn(true);
    Mockito.when(cognitoAdminClaim.getAdminClaim()).thenReturn(Optional.of("admin-claim"));
  }

  @Test
  @Transactional
  public void testCreateResource() throws DocumentSerializationException {
    final CreateGithubRepo resource = createResource(handler, resourceConverter);
    assertEquals("myrepo", resource.getGithubRepository());
  }
}
