package com.octopus.audits.domain.handlers;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.audits.domain.Constants;
import com.octopus.audits.domain.jsonapi.PagedResultsLinksBuilder;
import com.octopus.audits.domain.entities.Audit;
import com.octopus.audits.domain.exceptions.EntityNotFound;
import com.octopus.audits.domain.exceptions.InvalidInput;
import com.octopus.audits.domain.exceptions.Unauthorized;
import com.octopus.audits.domain.features.DisableSecurityFeature;
import com.octopus.audits.domain.utilities.JwtUtils;
import com.octopus.audits.domain.utilities.PartitionIdentifier;
import com.octopus.audits.domain.utilities.impl.JoseJwtVerifier;
import com.octopus.audits.domain.wrappers.FilteredResultWrapper;
import com.octopus.audits.infrastructure.repositories.AuditRepository;
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
 * Handlers take the raw input from the upstream service, like Lambda or a web server, convert the inputs to POJOs, apply the security rules, create an audit
 * trail, and then pass the requests down to repositories.
 */
@ApplicationScoped
public class AuditsHandler {

  /**
   * A retry policy used when accessing the database. Aurora serverless databases can take a bit of time
   * to wake up after sleeping, with testing shows it can take 30 seconds to make the initial requests.
   *
   * <p>Note though that requests to retrieve policies are usually made via an API Gateway, which has a timeout
   * of 29 seconds. This means the HTTP client (usually a browser) must retry read requests that timeout,
   * as any response by the service after 29 seconds is ignored by Api gateway.
   *
   * <p>This means read requests are retried for 15 seconds, which assumes we can take up half the request time
   * trying to warm up the database.
   */
  private static final RetryPolicy<FilteredResultWrapper<Audit>> RETRY_POLICY_GET_ALL = RetryPolicy
      .<FilteredResultWrapper<Audit>>builder()
      .handle(Exception.class)
      .withDelay(Duration.ofSeconds(5))
      .withMaxRetries(3)
      .build();

  /**
   * This is the retry policy for the request to get a single audit record. It retries for 15 seconds.
   */
  private static final RetryPolicy<Audit> RETRY_POLICY_GET_ONE = RetryPolicy
      .<Audit>builder()
      .handle(Exception.class)
      .withDelay(Duration.ofSeconds(5))
      .withMaxRetries(9)
      .build();

  /**
   * Write requests are assumed to be fire and forget, which means we are not bound by the API Gateway timeouts.
   * So we retry for up to 60 seconds to create a new record.
   */
  private static final RetryPolicy<Audit> RETRY_POLICY_CREATE_ONE = RetryPolicy
      .<Audit>builder()
      .handle(Exception.class)
      .abortOn(InvalidInput.class)
      .withDelay(Duration.ofSeconds(5))
      .withMaxRetries(12)
      .build();

  @ConfigProperty(name = "cognito.admin-claim")
  String cognitoAdminClaim;

  @ConfigProperty(name = "cognito.client-id")
  String cognitoClientId;

  @Inject
  DisableSecurityFeature cognitoDisableAuth;

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
      throw new Unauthorized();
    }

    final String partition = partitionIdentifier
        .getPartition(
            dataPartitionHeaders,
            jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader).orElse(null));

    final FilteredResultWrapper<Audit> audits = Failsafe.with(RETRY_POLICY_GET_ALL).get(() ->
        auditRepository.findAll(
            List.of(Constants.DEFAULT_PARTITION, partition),
            filterParam,
            pageOffset,
            pageLimit)
    );

    final JSONAPIDocument<List<Audit>> document = new JSONAPIDocument<List<Audit>>(
        audits.getList());

    PagedResultsLinksBuilder.generatePageLinks(document, pageLimit, pageOffset, audits);

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
      throw new Unauthorized();
    }

    final Audit audit = getResourceFromDocument(document);

    audit.dataPartition = partitionIdentifier.getPartition(
        dataPartitionHeaders,
        jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader).orElse(null));

    Failsafe.with(RETRY_POLICY_CREATE_ONE).run(() -> auditRepository.save(audit));

    return respondWithResource(audit);
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
      final String serviceAuthorizationHeader)
      throws DocumentSerializationException {
    if (!isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new Unauthorized();
    }

    final String partition = partitionIdentifier
        .getPartition(dataPartitionHeaders, jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader).orElse(null));

    final int parsedInt = Try.of(() -> Integer.parseInt(id))
        .getOrElseThrow(EntityNotFound::new);

    final Audit audit = Failsafe.with(RETRY_POLICY_GET_ONE)
        .get(() -> auditRepository.findOne(parsedInt));

    if (audit != null
        && (Constants.DEFAULT_PARTITION.equals(audit.getDataPartition())
        || StringUtils.equals(partition, audit.getDataPartition()))) {
      return respondWithResource(audit);
    }

    throw new EntityNotFound();
  }

  private Audit getResourceFromDocument(final String document) {
    try {
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
    } catch (final Exception ex) {
      // Assume the JSON is unable to be parsed.
      throw new InvalidInput();
    }
  }

  private String respondWithResource(final Audit audit)
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
