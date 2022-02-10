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
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
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

  @ConfigProperty(name = "cognito.disable-auth")
  Boolean cognitoDisableAuth;

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
   * @param acceptHeaders The "accept" headers.
   * @param filterParam The filter query param.
   * @return All matching resources
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI resource.
   */
  public String getAll(@NonNull final List<String> acceptHeaders,
      final String filterParam,
      final String authorizationHeader)
      throws DocumentSerializationException {
    if (!isAuthorized(authorizationHeader)) {
      throw new Unauthorized();
    }

    final List<Audit> audits =
        auditRepository.findAll(
            List.of(Constants.DEFAULT_PARTITION, partitionIdentifier.getPartition(acceptHeaders)),
            filterParam);
    final JSONAPIDocument<List<Audit>> document = new JSONAPIDocument<List<Audit>>(audits);
    final byte[] content = resourceConverter.writeDocumentCollection(document);
    return new String(content);
  }

  /**
   * Creates a new resource.
   *
   * @param document The JSONAPI resource to create.
   * @param acceptHeaders The "accept" headers.
   * @return The newly created resource
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI resource.
   */
  public String create(
      @NonNull final String document,
      @NonNull final List<String> acceptHeaders,
      final String authorizationHeader)
      throws DocumentSerializationException {

    if (!isAuthorized(authorizationHeader)) {
      throw new Unauthorized();
    }

    final Audit audit = getResourceFromDocument(document);

    audit.dataPartition = partitionIdentifier.getPartition(acceptHeaders);

    auditRepository.save(audit);

    return respondWithResource(audit);
  }

  /**
   * Returns the one resource that matches the supplied ID.
   *
   * @param id The ID of the resource to return.
   * @param acceptHeaders The "accept" headers.
   * @return The matching resource.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI resource.
   */
  public String getOne(@NonNull final String id,
      @NonNull final List<String> acceptHeaders,
      final String authorizationHeader)
      throws DocumentSerializationException {
    if (!isAuthorized(authorizationHeader)) {
      throw new Unauthorized();
    }

    try {
      final Audit audit = auditRepository.findOne(Integer.parseInt(id));
      if (audit != null
          && (Constants.DEFAULT_PARTITION.equals(audit.getDataPartition())
              || partitionIdentifier
                  .getPartition(acceptHeaders)
                  .equals(audit.getDataPartition()))) {
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
     The ID of a audit is determined by the URL, while the partition comes froms
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

  private boolean isAuthorized(final String authorizationHeader) {
    return cognitoDisableAuth || jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader)
        .map(jwt -> jwtVerifier.jwtContainsClaim(jwt, cognitoAdminClaim))
        .orElse(false);
  }
}
