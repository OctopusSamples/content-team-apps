package com.octopus.githubrepo.domain.utils.impl;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.exceptions.InvalidInput;
import com.octopus.githubrepo.domain.utils.JsonApiResourceUtils;
import java.nio.charset.StandardCharsets;
import lombok.NonNull;

/**
 * An implementation of JsonApiResourceUtils.
 * @param <T> The JSON API resource type.
 */
public class JsonApiResourceUtilsImpl<T> implements JsonApiResourceUtils<T> {

  private ResourceConverter resourceConverter;

  /**
   * Constructor.
   * @param resourceConverter The resource converted used to handle JSON documents.
   */
  public JsonApiResourceUtilsImpl(final ResourceConverter resourceConverter) {
    this.resourceConverter = resourceConverter;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T getResourceFromDocument(@NonNull final String document, @NonNull final Class<T> clazz) {
    try {
      final JSONAPIDocument<T> resourceDocument =
          resourceConverter.readDocument(document.getBytes(StandardCharsets.UTF_8), clazz);
      return resourceDocument.get();
    } catch (final Exception ex) {
      // Assume the JSON is unable to be parsed.
      throw new InvalidInput();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String respondWithResource(T resource) throws DocumentSerializationException {
    final JSONAPIDocument<T> document = new JSONAPIDocument<T>(resource);
    return new String(resourceConverter.writeDocument(document));
  }
}
