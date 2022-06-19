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
public class ResourceHandlerGetOne {

  private static final int DATABASE_RETRIES = 3;
  private static final int DATABASE_RETRY_DELAY = 5;

  /*
   * When connecting to AWS Aurora serverless, the first request will often fail with the
   * error "Acquisition timeout while waiting for new connection". This retry allows
   * us to handle these kind of situations.
   *
   * See https://aws.amazon.com/blogs/database/best-practices-for-working-with-amazon-aurora-serverless/
   * under the "Retry logic" section.
   */
  private static final RetryPolicy<Optional<String>> RETRY_POLICY = RetryPolicy
      .<Optional<String>>builder()
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


  /**
   * Returns the one resource that matches the supplied ID.
   *
   * @param id                   The ID of the resource to return.
   * @param dataPartitionHeaders The "data-partition" headers.
   * @return The matching resource.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI resource.
   */
  public String getOne(@NonNull final String id,
      @NonNull final List<String> dataPartitionHeaders,
      final String authorizationHeader,
      final String serviceAuthorizationHeader) {
    if (!authorizer.isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new UnauthorizedException();
    }

    final String partition = partitionIdentifier
        .getPartition(
            dataPartitionHeaders,
            jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader).orElse(null));

    final int parsedId = Try.of(() -> Integer.parseInt(id))
        .getOrElseThrow(() -> new EntityNotFoundException());

    final Optional<String> result =  Failsafe.with(RETRY_POLICY)
        .get(() -> {
          final Product product = repository.findOne(parsedId);
          if (product != null
              && (Constants.DEFAULT_PARTITION.equals(product.getDataPartition())
              || StringUtils.equals(partition, product.getDataPartition()))) {
            return Optional.of(respondWithResource(product));
          }
          return Optional.empty();
        });

    return result.orElseThrow(EntityNotFoundException::new);
  }

  private String respondWithResource(final Product product)
      throws DocumentSerializationException {
    final JSONAPIDocument<Product> document = new JSONAPIDocument<Product>(product);
    return new String(resourceConverter.writeDocument(document));
  }
}
