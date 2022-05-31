package com.octopus.githubrepo.domain.handlers.createcommit;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.githubrepo.domain.entities.CreateGithubCommit;
import com.octopus.githubrepo.domain.entities.Secret;
import com.octopus.githubrepo.domain.handlers.GitHubCommitHandler;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.NonNull;

public class BaseTest {
  protected CreateGithubCommit createResource() {
    return createResource(true);
  }

  protected CreateGithubCommit createResource(final boolean addSecrets) {
    final CreateGithubCommit resource = new CreateGithubCommit();
    resource.setGithubRepository("myrepo");
    if (addSecrets) {
      resource.setSecrets(List.of(
        Secret.builder().name("secret").value("secret").encrypted(false).build(),
        Secret.builder().name("secret4").value("").encrypted(false).build(),
        Secret.builder().name("").value("secret").encrypted(false).build(),
        Secret.builder().name("preserveme").value("value").preserveExistingSecret(true).build(),
        Secret.builder().name("secret3").value("secret3").encrypted(true).build()));
    }
    return resource;
  }

  protected CreateGithubCommit createResource(
      @NonNull final GitHubCommitHandler handler,
      @NonNull final ResourceConverter resourceConverter) throws DocumentSerializationException {
    return createResource(handler, resourceConverter, true);
  }

  protected CreateGithubCommit createResource(
      @NonNull final GitHubCommitHandler handler,
      @NonNull final ResourceConverter resourceConverter,
      final boolean addSecrets)
      throws DocumentSerializationException {
    final CreateGithubCommit resource = createResource(addSecrets);
    final String result =
        handler.create(
            resourceToResourceDocument(resourceConverter, resource),
            null, null, null, null, null,"blah");
    final CreateGithubCommit resultObject = getResourceFromDocument(resourceConverter, result);
    return resultObject;
  }

  protected CreateGithubCommit getResourceFromDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final String document) {
    final JSONAPIDocument<CreateGithubCommit> resourceDocument =
        resourceConverter.readDocument(document.getBytes(StandardCharsets.UTF_8), CreateGithubCommit.class);
    final CreateGithubCommit resource = resourceDocument.get();
    return resource;
  }

  protected List<CreateGithubCommit> getResourcesFromDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final String document) {
    final JSONAPIDocument<List<CreateGithubCommit>> resourceDocument =
        resourceConverter.readDocumentCollection(
            document.getBytes(StandardCharsets.UTF_8), CreateGithubCommit.class);
    final List<CreateGithubCommit> resources = resourceDocument.get();
    return resources;
  }

  protected String resourceToResourceDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final CreateGithubCommit audit)
      throws DocumentSerializationException {
    final JSONAPIDocument<CreateGithubCommit> document = new JSONAPIDocument<CreateGithubCommit>(audit);
    return new String(resourceConverter.writeDocument(document));
  }
}
