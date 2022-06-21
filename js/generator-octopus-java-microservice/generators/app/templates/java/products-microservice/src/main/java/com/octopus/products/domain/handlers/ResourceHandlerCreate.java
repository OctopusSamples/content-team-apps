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
public class ResourceHandlerCreate {

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
   * Creates a new resource.
   *
   * @param document             The JSONAPI resource to create.
   * @param dataPartitionHeaders The "Data-Partition" headers.
   * @return The newly created resource
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI resource.
   */
  public String create(
      @NonNull final String document,
      @NonNull final List<String> dataPartitionHeaders,
      final String authorizationHeader,
      final String serviceAuthorizationHeader)
      throws DocumentSerializationException {

    if (!authorizer.isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new UnauthorizedException();
    }

    final Product product = getResourceFromDocument(document);

    product.setDataPartition(partitionIdentifier.getPartition(
        dataPartitionHeaders,
        jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader).orElse(null)));

    Failsafe.with(RETRY_POLICY).run(() -> repository.save(product));

    return respondWithResource(product);
  }

  private Product getResourceFromDocument(final String document) {
    return Try.of(() -> {
      final JSONAPIDocument<Product> resourceDocument = resourceConverter.readDocument(document.getBytes(StandardCharsets.UTF_8), Product.class);
      final Product product = resourceDocument.get();
      /*
       The ID of a resource is determined by the URL, while the partition comes froms
       the headers. If either of these values was sent by the client, strip them out.
      */
      product.setId(null);
      product.setDataPartition(null);
      return product;
    })
    // Assume the exception relates to invalid input
    .getOrElseThrow(e -> new InvalidInputException());
  }

  private String respondWithResource(final Product product)
      throws DocumentSerializationException {
    final JSONAPIDocument<Product> document = new JSONAPIDocument<Product>(product);
    return new String(resourceConverter.writeDocument(document));
  }

}
