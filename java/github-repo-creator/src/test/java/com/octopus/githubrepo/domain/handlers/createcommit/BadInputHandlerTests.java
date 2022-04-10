package com.octopus.githubrepo.domain.handlers.createcommit;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.exceptions.InvalidInputException;
import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.entities.CreateGithubCommit;
import com.octopus.githubrepo.domain.handlers.GitHubCommitHandler;
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
  GitHubCommitHandler handler;

  @Inject
  ResourceConverter resourceConverter;

  @Test
  @Transactional
  public void testCreateBadResource() {
    final CreateGithubCommit resource = new CreateGithubCommit();
    assertThrows(InvalidInputException.class, () ->
        handler.create(
            resourceToResourceDocument(resourceConverter, resource),
            null, null, null, "blah"));
  }
}
