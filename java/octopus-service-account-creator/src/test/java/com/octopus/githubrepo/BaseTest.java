package com.octopus.githubrepo;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.githubrepo.domain.entities.ServiceAccount;
import com.octopus.githubrepo.domain.handlers.ServiceAccountHandler;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.NonNull;

public class BaseTest {
  protected ServiceAccount createResource(@NonNull final String name) {
    final ServiceAccount resource = new ServiceAccount();
    resource.setUsername(name);
    resource.setDisplayName("A description");
    return resource;
  }

  protected ServiceAccount createResource(
      @NonNull final ServiceAccountHandler handler,
      @NonNull final ResourceConverter resourceConverter)
      throws DocumentSerializationException {
    final ServiceAccount resource = createResource("myname");
    final String result =
        handler.create(
            resourceToResourceDocument(resourceConverter, resource),
            null, null, null);
    final ServiceAccount resultObject = getResourceFromDocument(resourceConverter, result);
    return resultObject;
  }

  protected ServiceAccount getResourceFromDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final String document) {
    final JSONAPIDocument<ServiceAccount> resourceDocument =
        resourceConverter.readDocument(document.getBytes(StandardCharsets.UTF_8), ServiceAccount.class);
    final ServiceAccount resource = resourceDocument.get();
    return resource;
  }

  protected List<ServiceAccount> getResourcesFromDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final String document) {
    final JSONAPIDocument<List<ServiceAccount>> resourceDocument =
        resourceConverter.readDocumentCollection(
            document.getBytes(StandardCharsets.UTF_8), ServiceAccount.class);
    final List<ServiceAccount> resources = resourceDocument.get();
    return resources;
  }

  protected String resourceToResourceDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final ServiceAccount audit)
      throws DocumentSerializationException {
    final JSONAPIDocument<ServiceAccount> document = new JSONAPIDocument<ServiceAccount>(audit);
    return new String(resourceConverter.writeDocument(document));
  }
}
