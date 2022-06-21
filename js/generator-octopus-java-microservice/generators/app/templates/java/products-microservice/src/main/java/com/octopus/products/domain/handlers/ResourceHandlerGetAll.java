package com.octopus.products.domain.handlers;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.exceptions.InvalidInputException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.AdminJwtGroupFeature;
import com.octopus.jsonapi.PagedResultsLinksBuilder;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import com.octopus.products.domain.Constants;
import com.octopus.products.domain.auth.Authorizer;
import com.octopus.products.domain.entities.Product;
import com.octopus.products.domain.features.impl.DisableSecurityFeatureImpl;
import com.octopus.products.infrastructure.repositories.ProductsRepository;
import com.octopus.utilties.PartitionIdentifier;
import com.octopus.wrappers.FilteredResultWrapper;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import io.vavr.control.Try;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Handlers take the raw input from the upstream service, like Lambda or a web server, convert the inputs to POJOs, apply the security rules, and then pass the
 * requests down to repositories.
 */
@ApplicationScoped
public class ResourceHandlerGetAll {

  private static final int DATABASE_RETRIES = 3;
  private static final int DATABASE_RETRY_DELAY = 5;

  private static final RetryPolicy<FilteredResultWrapper<Product>> GET_ALL_RETRY_POLICY = RetryPolicy
      .<FilteredResultWrapper<Product>>builder()
      .handle(Exception.class)
      .withDelay(Duration.ofSeconds(DATABASE_RETRY_DELAY))
      .withMaxRetries(DATABASE_RETRIES)
      .build();

  @Inject
  Authorizer authorizer;

  @Inject
  ProductsRepository repository;

  @Inject
  ResourceConverter resourceConverter;

  @Inject
  PartitionIdentifier partitionIdentifier;

  @Inject
  JwtUtils jwtUtils;

  @Inject
  PagedResultsLinksBuilder pagedResultsLinksBuilder;

  /**
   * Returns all matching resources.
   *
   * @param dataPartitionHeaders The "data-partition" headers.
   * @param filterParam          The filter query param.
   * @return All matching resources
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI resource.
   */
  public String getAll(@NonNull final List<String> dataPartitionHeaders,
      final String filterParam,
      final String pageOffset,
      final String pageLimit,
      final String authorizationHeader,
      final String serviceAuthorizationHeader)
      throws DocumentSerializationException {
    if (!authorizer.isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new UnauthorizedException();
    }

    final String partition = partitionIdentifier
        .getPartition(
            dataPartitionHeaders,
            jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader).orElse(null));

    final FilteredResultWrapper<Product> resources = Failsafe.with(GET_ALL_RETRY_POLICY)
        .get(() -> repository.findAll(
            List.of(Constants.DEFAULT_PARTITION, partition),
            filterParam,
            pageOffset,
            pageLimit));

    final JSONAPIDocument<List<Product>> document = new JSONAPIDocument<List<Product>>(
        resources.getList());

    pagedResultsLinksBuilder.generatePageLinks(document, pageLimit, pageOffset, resources, "customers");

    final byte[] content = resourceConverter.writeDocumentCollection(document);
    return new String(content);
  }
}
