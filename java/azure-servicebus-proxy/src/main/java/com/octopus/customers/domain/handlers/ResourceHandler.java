package com.octopus.customers.domain.handlers;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.customers.domain.entities.Customer;
import com.octopus.customers.domain.features.impl.DisableSecurityFeatureImpl;
import com.octopus.exceptions.InvalidInput;
import com.octopus.exceptions.Unauthorized;
import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.AdminJwtGroupFeature;
import com.octopus.jsonapi.PagedResultsLinksBuilder;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import com.octopus.utilties.PartitionIdentifier;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Handlers take the raw input from the upstream service, like Lambda or a web server, convert the
 * inputs to POJOs, apply the security rules, and then pass the requests down
 * to repositories.
 */
@ApplicationScoped
public class ResourceHandler {

  @Inject
  AdminJwtClaimFeature adminJwtClaimFeature;

  @ConfigProperty(name = "cognito.client-id")
  String cognitoClientId;

  @Inject
  DisableSecurityFeatureImpl cognitoDisableAuth;

  @Inject
  AdminJwtGroupFeature adminJwtGroupFeature;

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

    final Customer resource = getResourceFromDocument(document);

    resource.dataPartition = partitionIdentifier.getPartition(
        dataPartitionHeaders,
        jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader).orElse(null));

    return respondWithResource(resource);
  }

  private Customer getResourceFromDocument(final String document) {
    try {
      final JSONAPIDocument<Customer> resourceDocument =
          resourceConverter.readDocument(document.getBytes(StandardCharsets.UTF_8), Customer.class);
      final Customer resource = resourceDocument.get();
      /*
       The ID of a resource is determined by the URL, while the partition comes froms
       the headers. If either of these values was sent by the client, strip them out.
      */
      resource.id = null;
      resource.dataPartition = null;
      return resource;
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
