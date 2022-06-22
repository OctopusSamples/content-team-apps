package com.octopus.githubrepo.domain.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.githubrepo.GlobalConstants;
import com.octopus.githubrepo.domain.cognito.CognitoAccessTokenGenerator;
import com.octopus.githubrepo.domain.entities.Audit;
import com.octopus.githubrepo.domain.framework.producers.JsonApiConverter;
import com.octopus.githubrepo.infrastructure.clients.AuditClient;
import io.quarkus.logging.Log;
import io.vavr.control.Try;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * A service used to write audit events to the audits microservice.
 */
@ApplicationScoped
public class AuditGenerator {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @RestClient
  AuditClient auditClient;

  @Inject
  CognitoAccessTokenGenerator cognitoAccessTokenGenerator;

  @Inject
  DisableSecurityFeature disableSecurityFeature;

  @Inject
  JsonApiConverter jsonApiConverter;

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  /**
   * Create an audit event.
   *
   * @param audit                The details of the audit event.
   * @param routingHeaders       The "routing" headers to propagate with the request.
   * @param dataPartitionHeaders The "data-partition" headers to propagate with the request.
   * @param authHeaders          The "authorization" headers propagate with the request.
   */
  public void createAuditEvent(
      @NonNull final Audit audit,
      final String xrayId,
      @NonNull final String routingHeaders,
      @NonNull final String dataPartitionHeaders,
      @NonNull final String authHeaders) {

    final Try<String> authHeader = disableSecurityFeature.getCognitoAuthDisabled()
        ? Try.of(() -> "")
        : cognitoAccessTokenGenerator.getAccessToken();

    authHeader
        .andThenTry(auditAccessToken ->
            auditClient.createAudit(
                new String(jsonApiConverter.buildResourceConverter().writeDocument(
                    new JSONAPIDocument<>(audit))),
                StringUtils.defaultString(xrayId),
                routingHeaders,
                dataPartitionHeaders,
                authHeaders,
                "Bearer " + auditAccessToken,
                GlobalConstants.ASYNC_INVOCATION_TYPE))
        .onFailure(e -> {
          // Note the failure
          Log.error(microserviceNameFeature.getMicroserviceName() + "-Audit-Failed", e);
          // As a fallback, write the audit event to the logs
          Try.run(() -> Log.error(OBJECT_MAPPER.writer().writeValueAsString(audit)));
        });
  }


}
