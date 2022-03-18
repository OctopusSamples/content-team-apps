package com.octopus.customers;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.customers.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.customers.domain.handlers.ResourceHandler;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.NonNull;

public class BaseTest {
  protected GithubUserLoggedInForFreeToolsEventV1 createResource(@NonNull final String email) {
    return GithubUserLoggedInForFreeToolsEventV1
        .builder()
        .emailAddress(email)
        .build();
  }

  protected void createResource(
      @NonNull final ResourceHandler handler,
      @NonNull final ResourceConverter resourceConverter,
      @NonNull final String partition)
      throws DocumentSerializationException {
    final GithubUserLoggedInForFreeToolsEventV1 resource = createResource("myname");
    handler.create(
        resourceToResourceDocument(resourceConverter, resource),
        List.of(partition),
        null,
        null,
        null);
  }

  protected String resourceToResourceDocument(
      @NonNull final ResourceConverter resourceConverter,
      @NonNull final GithubUserLoggedInForFreeToolsEventV1 resource)
      throws DocumentSerializationException {
    final JSONAPIDocument<GithubUserLoggedInForFreeToolsEventV1> document = new JSONAPIDocument<>(resource);
    return new String(resourceConverter.writeDocument(document));
  }
}
