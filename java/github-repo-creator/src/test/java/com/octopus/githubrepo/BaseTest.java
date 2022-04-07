package com.octopus.githubrepo;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.githubrepo.domain.entities.CreateGithubRepo;
import com.octopus.githubrepo.domain.entities.Secret;
import com.octopus.githubrepo.domain.handlers.GitHubRepoHandler;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.NonNull;

public class BaseTest {
  protected CreateGithubRepo createResource() {
    return createResource(true);
  }

  protected CreateGithubRepo createResource(final boolean addSecrets) {
    final CreateGithubRepo resource = new CreateGithubRepo();
    resource.setGithubRepository("myrepo");
    if (addSecrets) {
      resource.setSecrets(List.of(
        Secret.builder().name("secret").value("secret").encrypted(false).build(),
        Secret.builder().name("secret4").value("").encrypted(false).build(),
        Secret.builder().name("").value("secret").encrypted(false).build(),
        Secret.builder().name("secret2").value("secret2").preserveExistingSecret(true).build(),
        Secret.builder().name("secret3").value("secret3").encrypted(true).build()));
    }
    return resource;
  }

  protected CreateGithubRepo createResource(
      @NonNull final GitHubRepoHandler handler,
      @NonNull final ResourceConverter resourceConverter) throws DocumentSerializationException {
    return createResource(handler, resourceConverter, true);
  }

  protected CreateGithubRepo createResource(
      @NonNull final GitHubRepoHandler handler,
      @NonNull final ResourceConverter resourceConverter,
      final boolean addSecrets)
      throws DocumentSerializationException {
    final CreateGithubRepo resource = createResource(addSecrets);
    final String result =
        handler.create(
            resourceToResourceDocument(resourceConverter, resource),
            null, null, null, "blah");
    final CreateGithubRepo resultObject = getResourceFromDocument(resourceConverter, result);
    return resultObject;
  }

  protected CreateGithubRepo getResourceFromDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final String document) {
    final JSONAPIDocument<CreateGithubRepo> resourceDocument =
        resourceConverter.readDocument(document.getBytes(StandardCharsets.UTF_8), CreateGithubRepo.class);
    final CreateGithubRepo resource = resourceDocument.get();
    return resource;
  }

  protected List<CreateGithubRepo> getResourcesFromDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final String document) {
    final JSONAPIDocument<List<CreateGithubRepo>> resourceDocument =
        resourceConverter.readDocumentCollection(
            document.getBytes(StandardCharsets.UTF_8), CreateGithubRepo.class);
    final List<CreateGithubRepo> resources = resourceDocument.get();
    return resources;
  }

  protected String resourceToResourceDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final CreateGithubRepo audit)
      throws DocumentSerializationException {
    final JSONAPIDocument<CreateGithubRepo> document = new JSONAPIDocument<CreateGithubRepo>(audit);
    return new String(resourceConverter.writeDocument(document));
  }
}
