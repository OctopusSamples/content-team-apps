package com.octopus.githubrepo.domain.handlers.createcommit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.entities.CreateGithubCommit;
import com.octopus.githubrepo.domain.entities.PopulateGithubRepo;
import com.octopus.githubrepo.domain.features.DisableServiceFeature;
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

/**
 * Simulate tests when the service is effectively disabled.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class DisabledHandlerTest extends BaseTest {

  @InjectMock
  DisableServiceFeature disableServiceFeature ;

  @Inject
  GitHubCommitHandler handler;

  @Inject
  ResourceConverter resourceConverter;


  @BeforeAll
  public void setup()  {
    Mockito.when(disableServiceFeature.getDisableRepoCreation()).thenReturn(true);
  }

  @Test
  @Transactional
  public void testCreateResource() throws DocumentSerializationException {
    final CreateGithubCommit resource = createResource(handler, resourceConverter);
    assertEquals("", resource.getGithubRepository());
    assertEquals("", resource.getId());
  }
}
