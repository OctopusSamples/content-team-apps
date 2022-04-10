package com.octopus.githubrepo.domain.handlers.createcommit;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.githubrepo.domain.handlers.populaterepo.BaseTest;
import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.entities.PopulateGithubRepo;
import com.octopus.githubrepo.domain.handlers.GitHubCommitHandler;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class HandlerAuthorizedTests extends BaseTest {

  @Inject
  GitHubCommitHandler handler;

  @Inject
  ResourceConverter resourceConverter;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @BeforeAll
  public void setup()  {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(false);
  }

  @Test
  @Transactional
  public void testCreateResource() {
    assertThrows(UnauthorizedException.class, () -> handler.create(
        resourceToResourceDocument(resourceConverter, new PopulateGithubRepo()),
        null, null, null, "blah"));
  }
}
