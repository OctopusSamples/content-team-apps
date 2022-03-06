package com.octopus.serviceaccount.domain.utils;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;

/**
 * A service that extracts resources from requests and builds JSON api responses.
 * @param <T> The resource type.
 */
public interface JsonApiResourceUtils<T> {

  /**
   * Extract the resource from the JSON API request.
   * @param document The HTTP body.
   * @param clazz The resource class.
   * @return The JSON API resource in the HTTP body.
   */
  T getResourceFromDocument(final String document, final Class<T> clazz);

  /**
   * Create a JSON response from the supplied resource.
   * @param resource The resource top include in the response.
   * @return The HTTP response body.
   */
  String respondWithResource(final T resource) throws DocumentSerializationException;
}
