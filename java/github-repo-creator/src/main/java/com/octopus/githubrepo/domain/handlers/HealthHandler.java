package com.octopus.githubrepo.domain.handlers;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.githubrepo.domain.entities.Health;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;

/**
 * Handles the health check requests.
 */
@ApplicationScoped
public class HealthHandler {

  @Inject
  ResourceConverter resourceConverter;

  /**
   * Get the health check response content.
   *
   * @param path   The path that was checked.
   * @param method The method that was checked.
   * @return The JSONAPI response representing the health check.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *                                        resource.
   */
  public String getHealth(@NonNull final String path, @NonNull final String method)
      throws DocumentSerializationException {

    return respondWithHealth(
        Health.builder()
            .status("OK")
            .path(path)
            .method(method)
            .endpoint(path + "/" + method)
            .build());
  }

  private String respondWithHealth(final Health health)
      throws DocumentSerializationException {
    final JSONAPIDocument<Health> document = new JSONAPIDocument<>(health);
    return new String(resourceConverter.writeDocument(document));
  }
}
