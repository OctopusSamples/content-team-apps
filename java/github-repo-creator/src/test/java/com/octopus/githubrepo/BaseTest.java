package com.octopus.githubrepo;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.githubrepo.domain.entities.PopulateGithubRepo;
import com.octopus.githubrepo.domain.entities.Secret;
import com.octopus.githubrepo.domain.handlers.GitHubRepoHandler;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.NonNull;

public class BaseTest {
  protected PopulateGithubRepo createResource() {
    return createResource(true);
  }

  protected PopulateGithubRepo createResource(final boolean addSecrets) {
    final PopulateGithubRepo resource = new PopulateGithubRepo();
    resource.setGithubRepository("myrepo");
    resource.setBranch("main");
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

  protected PopulateGithubRepo createResource(
      @NonNull final GitHubRepoHandler handler,
      @NonNull final ResourceConverter resourceConverter) throws DocumentSerializationException {
    return createResource(handler, resourceConverter, true);
  }

  protected PopulateGithubRepo createResource(
      @NonNull final GitHubRepoHandler handler,
      @NonNull final ResourceConverter resourceConverter,
      final boolean addSecrets)
      throws DocumentSerializationException {
    final PopulateGithubRepo resource = createResource(addSecrets);
    final String result =
        handler.create(
            resourceToResourceDocument(resourceConverter, resource),
            null, null, null, "blah");
    final PopulateGithubRepo resultObject = getResourceFromDocument(resourceConverter, result);
    return resultObject;
  }

  protected PopulateGithubRepo getResourceFromDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final String document) {
    final JSONAPIDocument<PopulateGithubRepo> resourceDocument =
        resourceConverter.readDocument(document.getBytes(StandardCharsets.UTF_8), PopulateGithubRepo.class);
    final PopulateGithubRepo resource = resourceDocument.get();
    return resource;
  }

  protected List<PopulateGithubRepo> getResourcesFromDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final String document) {
    final JSONAPIDocument<List<PopulateGithubRepo>> resourceDocument =
        resourceConverter.readDocumentCollection(
            document.getBytes(StandardCharsets.UTF_8), PopulateGithubRepo.class);
    final List<PopulateGithubRepo> resources = resourceDocument.get();
    return resources;
  }

  protected String resourceToResourceDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final PopulateGithubRepo audit)
      throws DocumentSerializationException {
    final JSONAPIDocument<PopulateGithubRepo> document = new JSONAPIDocument<PopulateGithubRepo>(audit);
    return new String(resourceConverter.writeDocument(document));
  }
}
