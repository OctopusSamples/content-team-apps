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
public class ResourceHandler {

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

  private static final RetryPolicy<FilteredResultWrapper<Product>> GET_ALL_RETRY_POLICY = RetryPolicy
      .<FilteredResultWrapper<Product>>builder()
      .handle(Exception.class)
      .withDelay(Duration.ofSeconds(DATABASE_RETRY_DELAY))
      .withMaxRetries(DATABASE_RETRIES)
      .build();

  @Inject
  AdminJwtClaimFeature adminJwtClaimFeature;

  @ConfigProperty(name = "cognito.client-id")
  String cognitoClientId;

  @Inject
  DisableSecurityFeatureImpl cognitoDisableAuth;

  @Inject
  AdminJwtGroupFeature adminJwtGroupFeature;

  @Inject
  ProductsRepository repository;

  @Inject
  ResourceConverter resourceConverter;

  @Inject
  PartitionIdentifier partitionIdentifier;

  @Inject
  JwtInspector jwtInspector;

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
    if (!isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
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

    if (!isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new UnauthorizedException();
    }

    final Product product = getResourceFromDocument(document);

    product.setDataPartition(partitionIdentifier.getPartition(
        dataPartitionHeaders,
        jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader).orElse(null)));

    Failsafe.with(RETRY_POLICY).run(() -> repository.save(product));

    return respondWithResource(product);
  }

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
    if (!isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new UnauthorizedException();
    }

    final String partition = partitionIdentifier
        .getPartition(
            dataPartitionHeaders,
            jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader).orElse(null));

    final Optional<String> result =  Failsafe.with(RETRY_POLICY)
        .get(() -> {
          final Product product = repository.findOne(Integer.parseInt(id));
          if (product != null
              && (Constants.DEFAULT_PARTITION.equals(product.getDataPartition())
              || StringUtils.equals(partition, product.getDataPartition()))) {
            return Optional.of(respondWithResource(product));
          }
          return Optional.empty();
        });

    return result.orElseThrow(EntityNotFoundException::new);
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

  /**
   * Determines if the supplied token grants the required scopes to execute the operation.
   *
   * @param authorizationHeader        The Authorization header.
   * @param serviceAuthorizationHeader The Service-Authorization header.
   * @return true if the request is authorized, and false otherwise.
   */
  private boolean isAuthorized(
      final String authorizationHeader,
      final String serviceAuthorizationHeader) {

    /*
      This method implements the following logic:
      * If auth is disabled, return true.
      * If the Service-Authorization header contains an access token with the correct scope,
        generated by a known app client, return true.
      * If the Authorization header contains a known group, return true.
      * Otherwise, return false.
     */

    if (cognitoDisableAuth.getCognitoAuthDisabled()) {
      return true;
    }

    /*
      An admin scope granted to an access token generated by a known client credentials
      app client is accepted as machine-to-machine communication.
     */
    if (adminJwtClaimFeature.getAdminClaim().isPresent() && jwtUtils.getJwtFromAuthorizationHeader(
            serviceAuthorizationHeader)
        .map(jwt -> jwtInspector.jwtContainsScope(jwt, adminJwtClaimFeature.getAdminClaim().get(), cognitoClientId))
        .orElse(false)) {
      return true;
    }

    /*
      Anyone assigned to the appropriate group is also granted access.
     */
    return adminJwtGroupFeature.getAdminGroup().isPresent() && jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader)
        .map(jwt -> jwtInspector.jwtContainsCognitoGroup(jwt, adminJwtGroupFeature.getAdminGroup().get()))
        .orElse(false);
  }
}
