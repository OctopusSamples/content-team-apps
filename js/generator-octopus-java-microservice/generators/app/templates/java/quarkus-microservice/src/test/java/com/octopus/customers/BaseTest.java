package com.octopus.customers;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.customers.domain.entities.Customer;
import com.octopus.customers.domain.handlers.CustomersHandler;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.NonNull;

public class BaseTest {
  protected Customer createResource(@NonNull final String subject) {
    return createResource(subject, null);
  }

  protected Customer createResource(@NonNull final String name, final String partition) {
    final Customer resource = new Customer();
    resource.setFirstName(name);
    resource.setLastName("Doe");
    resource.setAddressLine1("1 Octopus St");
    resource.setAddressLine2("Coral Garden");
    resource.setCity("Brisbane");
    return resource;
  }

  protected Customer createResource(
      @NonNull final CustomersHandler handler,
      @NonNull final ResourceConverter resourceConverter,
      @NonNull final String partition)
      throws DocumentSerializationException {
    final Customer resource = createResource("myname");
    final String result =
        handler.create(
            resourceToResourceDocument(resourceConverter, resource),
            List.of(partition),
            null, null);
    final Customer resultObject = getResourceFromDocument(resourceConverter, result);
    return resultObject;
  }

  protected Customer getResourceFromDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final String document) {
    final JSONAPIDocument<Customer> resourceDocument =
        resourceConverter.readDocument(document.getBytes(StandardCharsets.UTF_8), Customer.class);
    final Customer resource = resourceDocument.get();
    return resource;
  }

  protected List<Customer> getResourcesFromDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final String document) {
    final JSONAPIDocument<List<Customer>> resourceDocument =
        resourceConverter.readDocumentCollection(
            document.getBytes(StandardCharsets.UTF_8), Customer.class);
    final List<Customer> resources = resourceDocument.get();
    return resources;
  }

  protected String resourceToResourceDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final Customer resource)
      throws DocumentSerializationException {
    final JSONAPIDocument<Customer> document = new JSONAPIDocument<Customer>(resource);
    return new String(resourceConverter.writeDocument(document));
  }
}
