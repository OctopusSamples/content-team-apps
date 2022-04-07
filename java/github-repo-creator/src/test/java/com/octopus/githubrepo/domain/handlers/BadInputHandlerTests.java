package com.octopus.githubrepo.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.exceptions.InvalidInputException;
import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.entities.CreateGithubRepo;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class BadInputHandlerTests extends BaseGitHubTest {

  @Inject
  GitHubRepoHandler handler;

  @Inject
  ResourceConverter resourceConverter;

  @Test
  @Transactional
  public void testCreateBadResource() {
    final CreateGithubRepo resource = new CreateGithubRepo();
    assertThrows(InvalidInputException.class, () ->
        handler.create(
            resourceToResourceDocument(resourceConverter, resource),
            null, null, null, "blah"));
  }
}
