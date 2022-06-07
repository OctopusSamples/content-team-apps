package com.octopus.products.domain.handlers;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.products.domain.Constants;
import com.octopus.products.domain.entities.Health;
import com.octopus.products.infrastructure.repositories.ProductsRepository;
import java.util.List;
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

  @Inject
  ProductsRepository repository;

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

    // Query for an empty set of results. This validates a connection to the database.
    repository.findAll(List.of(Constants.DEFAULT_PARTITION), null, "0", "0");

    return respondWithHealth(
        Health.builder()
            .status("OK")
            .path(path)
            .method(method)
            .endpoint(path + "/" + method)
            .build());
  }

  private String respondWithHealth(final Health health) throws DocumentSerializationException {
    final JSONAPIDocument<Health> document = new JSONAPIDocument<>(health);
    return new String(resourceConverter.writeDocument(document));
  }
}
