package com.octopus.customers.domain.handlers;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.customers.domain.Constants;
import com.octopus.customers.domain.entities.Customer;
import com.octopus.customers.domain.features.DisableSecurityFeatureImpl;
import com.octopus.customers.infrastructure.repositories.CustomersRepository;
import com.octopus.exceptions.EntityNotFound;
import com.octopus.exceptions.InvalidInput;
import com.octopus.exceptions.Unauthorized;
import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.AdminJwtGroupFeature;
import com.octopus.jsonapi.PagedResultsLinksBuilder;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import com.octopus.utilties.PartitionIdentifier;
import com.octopus.wrappers.FilteredResultWrapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Handlers take the raw input from the upstream service, like Lambda or a web server, convert the
 * inputs to POJOs, apply the security rules, create an audit trail, and then pass the requests down
 * to repositories.
 */
@ApplicationScoped
public class CustomersHandler {

  @Inject
  AdminJwtClaimFeature adminJwtClaimFeature;

  @ConfigProperty(name = "cognito.client-id")
  String cognitoClientId;

  @Inject
  DisableSecurityFeatureImpl cognitoDisableAuth;

  @Inject
  AdminJwtGroupFeature adminJwtGroupFeature;

  @Inject
  CustomersRepository auditRepository;

  @Inject
  ResourceConverter resourceConverter;

  @Inject
  PartitionIdentifier partitionIdentifier;

  @Inject
  JwtInspector JwtInspector;

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
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *                                        resource.
   */
  public String getAll(@NonNull final List<String> dataPartitionHeaders,
      final String filterParam,
      final String pageOffset,
      final String pageLimit,
      final String authorizationHeader,
      final String serviceAuthorizationHeader)
      throws DocumentSerializationException {
    if (!isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new Unauthorized();
    }

    final String partition = partitionIdentifier
        .getPartition(
            dataPartitionHeaders,
            jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader).orElse(null));

    final FilteredResultWrapper<Customer> audits =
        auditRepository.findAll(
            List.of(Constants.DEFAULT_PARTITION, partition),
            filterParam,
            pageOffset,
            pageLimit);
    final JSONAPIDocument<List<Customer>> document = new JSONAPIDocument<List<Customer>>(
        audits.getList());

    pagedResultsLinksBuilder.generatePageLinks(document, pageLimit, pageOffset, audits);

    final byte[] content = resourceConverter.writeDocumentCollection(document);
    return new String(content);
  }

  /**
   * Creates a new resource.
   *
   * @param document             The JSONAPI resource to create.
   * @param dataPartitionHeaders The "Data-Partition" headers.
   * @return The newly created resource
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *                                        resource.
   */
  public String create(
      @NonNull final String document,
      @NonNull final List<String> dataPartitionHeaders,
      final String authorizationHeader,
      final String serviceAuthorizationHeader)
      throws DocumentSerializationException {

    if (!isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new Unauthorized();
    }

    final Customer customer = getResourceFromDocument(document);

    customer.dataPartition = partitionIdentifier.getPartition(
        dataPartitionHeaders,
        jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader).orElse(null));

    auditRepository.save(customer);

    return respondWithResource(customer);
  }

  /**
   * Returns the one resource that matches the supplied ID.
   *
   * @param id                   The ID of the resource to return.
   * @param dataPartitionHeaders The "data-partition" headers.
   * @return The matching resource.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *                                        resource.
   */
  public String getOne(@NonNull final String id,
      @NonNull final List<String> dataPartitionHeaders,
      final String authorizationHeader,
      final String serviceAuthorizationHeader)
      throws DocumentSerializationException {
    if (!isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new Unauthorized();
    }

    final String partition = partitionIdentifier
        .getPartition(
            dataPartitionHeaders,
            jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader).orElse(null));

    try {
      final Customer customer = auditRepository.findOne(Integer.parseInt(id));
      if (customer != null
          && (Constants.DEFAULT_PARTITION.equals(customer.getDataPartition())
          || StringUtils.equals(partition, customer.getDataPartition()))) {
        return respondWithResource(customer);
      }
    } catch (final NumberFormatException ex) {
      // ignored, as the supplied id was not an int, and would never find any entities
    }
    throw new EntityNotFound();
  }

  private Customer getResourceFromDocument(final String document) {
    try {
      final JSONAPIDocument<Customer> resourceDocument =
          resourceConverter.readDocument(document.getBytes(StandardCharsets.UTF_8), Customer.class);
      final Customer customer = resourceDocument.get();
      /*
       The ID of an audit is determined by the URL, while the partition comes froms
       the headers. If either of these values was sent by the client, strip them out.
      */
      customer.id = null;
      customer.dataPartition = null;
      return customer;
    } catch (final Exception ex) {
      // Assume the JSON is unable to be parsed.
      throw new InvalidInput();
    }
  }

  private String respondWithResource(final Customer customer)
      throws DocumentSerializationException {
    final JSONAPIDocument<Customer> document = new JSONAPIDocument<Customer>(customer);
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
        .map(jwt -> JwtInspector.jwtContainsScope(jwt, adminJwtClaimFeature.getAdminClaim().get(), cognitoClientId))
        .orElse(false)) {
      return true;
    }

    /*
      Anyone assigned to the appropriate group is also granted access.
     */
    return adminJwtGroupFeature.getAdminGroup().isPresent() && jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader)
        .map(jwt -> JwtInspector.jwtContainsCognitoGroup(jwt, adminJwtGroupFeature.getAdminGroup().get()))
        .orElse(false);
  }
}
