package com.octopus.audits.domain.handlers;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.audits.domain.Constants;
import com.octopus.audits.domain.entities.Audit;
import com.octopus.audits.domain.exceptions.EntityNotFound;
import com.octopus.audits.domain.exceptions.Unauthorized;
import com.octopus.audits.domain.utilities.JwtUtils;
import com.octopus.audits.domain.utilities.PartitionIdentifier;
import com.octopus.audits.domain.utilities.impl.JoseJwtVerifier;
import com.octopus.audits.infrastructure.repositories.AuditRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
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
public class AuditsHandler {

  @ConfigProperty(name = "cognito.admin-claim")
  String cognitoAdminClaim;

  @ConfigProperty(name = "cognito.client-id")
  String cognitoClientId;

  @ConfigProperty(name = "cognito.disable-auth")
  Boolean cognitoDisableAuth;

  @ConfigProperty(name = "cognito.admin-group")
  Optional<String> adminGroup;

  @Inject
  AuditRepository auditRepository;

  @Inject
  ResourceConverter resourceConverter;

  @Inject
  PartitionIdentifier partitionIdentifier;

  @Inject
  JoseJwtVerifier jwtVerifier;

  @Inject
  JwtUtils jwtUtils;

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

    final List<Audit> audits =
        auditRepository.findAll(
            List.of(Constants.DEFAULT_PARTITION, partition),
            filterParam,
            pageOffset,
            pageLimit);
    final JSONAPIDocument<List<Audit>> document = new JSONAPIDocument<List<Audit>>(audits);
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

    final Audit audit = getResourceFromDocument(document);

    audit.dataPartition = partitionIdentifier.getPartition(
        dataPartitionHeaders,
        jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader).orElse(null));

    auditRepository.save(audit);

    return respondWithResource(audit);
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
      final Audit audit = auditRepository.findOne(Integer.parseInt(id));
      if (audit != null
          && (Constants.DEFAULT_PARTITION.equals(audit.getDataPartition())
          || StringUtils.equals(partition, audit.getDataPartition()))) {
        return respondWithResource(audit);
      }
    } catch (final NumberFormatException ex) {
      // ignored, as the supplied id was not an int, and would never find any entities
    }
    throw new EntityNotFound();
  }

  private Audit getResourceFromDocument(@NonNull final String document) {
    final JSONAPIDocument<Audit> resourceDocument =
        resourceConverter.readDocument(document.getBytes(StandardCharsets.UTF_8), Audit.class);
    final Audit audit = resourceDocument.get();
    /*
     The ID of an audit is determined by the URL, while the partition comes froms
     the headers. If either of these values was sent by the client, strip them out.
    */
    audit.id = null;
    audit.dataPartition = null;
    return audit;
  }

  private String respondWithResource(@NonNull final Audit audit)
      throws DocumentSerializationException {
    final JSONAPIDocument<Audit> document = new JSONAPIDocument<Audit>(audit);
    return new String(resourceConverter.writeDocument(document));
  }

  /**
   * Determines if the supplied token grants the required scopes to execute the operation.
   *
   * @param authorizationHeader        The Authorization header.
   * @param serviceAuthorizationHeader The Service-Authorization header.
   * @return true if the request is authorized, and false otherwise.
   */
  private boolean isAuthorized(final String authorizationHeader,
      final String serviceAuthorizationHeader) {

    /*
      This method implements the following logic:
      * If auth is disabled, return true.
      * If the Service-Authorization header contains an access token with the correct scope,
        generated by a known app client, return true.
      * If the Authorization header contains a known group, return true.
      * Otherwise, return false.
     */

    if (cognitoDisableAuth) {
      return true;
    }

    /*
      An admin scope granted to an access token generated by a known client credentials
      app client is accepted as machine-to-machine communication.
     */
    if (jwtUtils.getJwtFromAuthorizationHeader(serviceAuthorizationHeader)
        .map(jwt -> jwtVerifier.jwtContainsScope(jwt, cognitoAdminClaim, cognitoClientId))
        .orElse(false)) {
      return true;
    }

    /*
      Anyone assigned to the appropriate group is also granted access.
     */
    return adminGroup.isPresent() && jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader)
        .map(jwt -> jwtVerifier.jwtContainsCognitoGroup(jwt, adminGroup.get()))
        .orElse(false);
  }
}
