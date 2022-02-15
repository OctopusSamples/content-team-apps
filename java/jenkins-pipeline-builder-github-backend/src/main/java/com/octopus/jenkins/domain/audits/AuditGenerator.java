package com.octopus.jenkins.domain.audits;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.octopus.jenkins.GlobalConstants;
import com.octopus.jenkins.domain.entities.Audit;
import com.octopus.jenkins.domain.framework.jsonapi.JsonApiConverter;
import com.octopus.jenkins.infrastructure.client.AuditClient;
import com.octopus.jenkins.infrastructure.client.CognitoClient;
import io.quarkus.logging.Log;
import io.vavr.control.Try;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * A service used to write audit events to the audits microservice.
 */
@ApplicationScoped
public class AuditGenerator {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static long expiry;
  private static String accessToken;

  @RestClient
  AuditClient auditClient;

  @RestClient
  CognitoClient cognitoClient;

  @Inject
  JsonApiConverter jsonApiConverter;

  @ConfigProperty(name = "cognito.client-id")
  Optional<String> cognitoClientId;

  @ConfigProperty(name = "cognito.client-secret")
  Optional<String> cognitoClientSecret;

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
      @NonNull final String routingHeaders,
      @NonNull final String dataPartitionHeaders,
      @NonNull final String authHeaders) {

    getAccessToken()
        .andThenTry(auditAccessToken ->
            auditClient.createAudit(
                new String(jsonApiConverter.buildResourceConverter().writeDocument(
                    new JSONAPIDocument<>(audit))),
                routingHeaders,
                dataPartitionHeaders,
                authHeaders,
                "Bearer " + auditAccessToken,
                GlobalConstants.ASYNC_INVOCATION_TYPE))
        .onFailure(e -> {
          // Note the failure
          Log.error(GlobalConstants.MICROSERVICE_NAME + "-Audit-Failed", e);
          // As a fallback, write the audit event to the logs
          Try.run(() -> Log.error(OBJECT_MAPPER.writer().writeValueAsString(audit)));
        });
  }

  private Try<String> getAccessToken() {
    if (!StringUtils.isEmpty(accessToken) && new Date().getTime() < expiry) {
      return Try.of(() -> accessToken);
    }

    if (cognitoClientId.isPresent() && cognitoClientSecret.isPresent()) {
      return Try.of(() -> cognitoClient.getToken(
              "Basic " + Base64.getEncoder()
                  .encodeToString(
                      (cognitoClientId.get() + ":" + cognitoClientSecret.get()).getBytes()),
              GlobalConstants.CLIENT_CREDENTIALS,
              cognitoClientId.get(),
              GlobalConstants.AUDIT_SCOPE))
          // We expect to see an access token. Fail if the value is empty.
          .filter(oauth -> StringUtils.isNotEmpty(oauth.getAccessToken()))
          // We can reuse a token for an hour, but we set the expiry 10 mins before just to be safe.
          .mapTry(oauth -> {
            accessToken = oauth.getAccessToken();
            expiry = new Date().getTime() + ((long) oauth.getExpiresIn() * 1000) - (10 * 60 * 1000);
            return accessToken;
          });
    }

    return Try.failure(new Exception("Cognito client ID or secret were not set"));
  }

}
