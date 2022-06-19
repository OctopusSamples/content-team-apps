package com.octopus.products;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.products.domain.entities.Product;
import com.octopus.products.domain.handlers.ResourceHandlerCreate;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.NonNull;

public class BaseTest {
  protected Product createResource(@NonNull final String subject) {
    return createResource(subject, null);
  }

  protected Product createResource(@NonNull final String name, final String partition) {
    final Product resource = new Product();
    resource.setName(name);
    resource.setDataPartition(partition);
    resource.setPdf("http://example.org/pdf");
    resource.setImage("http://example.org/image");
    resource.setEpub("http://example.org/epub");
    return resource;
  }

  protected Product createResource(
      @NonNull final ResourceHandlerCreate handler,
      @NonNull final ResourceConverter resourceConverter,
      @NonNull final String partition)
      throws DocumentSerializationException {
    final Product resource = createResource("myname");
    final String result =
        handler.create(
            resourceToResourceDocument(resourceConverter, resource),
            List.of(partition),
            null, null);
    final Product resultObject = getResourceFromDocument(resourceConverter, result);
    return resultObject;
  }

  protected Product getResourceFromDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final String document) {
    final JSONAPIDocument<Product> resourceDocument =
        resourceConverter.readDocument(document.getBytes(StandardCharsets.UTF_8), Product.class);
    final Product resource = resourceDocument.get();
    return resource;
  }

  protected List<Product> getResourcesFromDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final String document) {
    final JSONAPIDocument<List<Product>> resourceDocument =
        resourceConverter.readDocumentCollection(
            document.getBytes(StandardCharsets.UTF_8), Product.class);
    final List<Product> resources = resourceDocument.get();
    return resources;
  }

  protected String resourceToResourceDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final Product resource)
      throws DocumentSerializationException {
    final JSONAPIDocument<Product> document = new JSONAPIDocument<Product>(resource);
    return new String(resourceConverter.writeDocument(document));
  }
}
