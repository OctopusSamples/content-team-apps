package com.octopus.githubrepo.domain.handlers.populaterepo;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.exceptions.InvalidInputException;
import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.entities.PopulateGithubRepo;
import com.octopus.githubrepo.domain.handlers.GitHubRepoHandler;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class BadInputHandlerTests extends BaseTest {

  @Inject
  GitHubRepoHandler handler;

  @Inject
  ResourceConverter resourceConverter;

  @Test
  @Transactional
  public void testCreateBadResource() {
    final PopulateGithubRepo resource = new PopulateGithubRepo();
    assertThrows(InvalidInputException.class, () ->
        handler.create(
            resourceToResourceDocument(resourceConverter, resource),
            null, null, null, "blah"));
  }
}
