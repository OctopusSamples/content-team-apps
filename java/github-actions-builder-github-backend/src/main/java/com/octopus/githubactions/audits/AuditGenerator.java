package com.octopus.githubactions.audits;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.octopus.githubactions.GlobalConstants;
import com.octopus.githubactions.client.AuditClient;
import com.octopus.githubactions.client.CognitoClient;
import com.octopus.githubactions.entities.Audit;
import com.octopus.githubactions.entities.Oauth;
import com.octopus.githubactions.jsonapi.JsonApiConverter;
import io.quarkus.logging.Log;
import io.vavr.control.Try;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * A service used to write audit events to the audits microservice.
 */
@ApplicationScoped
public class AuditGenerator {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
   * @param audit         The details of the audit event.
   * @param acceptHeaders The "accept" headers to propagate with the request.
   * @param authHeaders   The "authorization" headers propagate with the request.
   */
  public void createAuditEvent(
      @NonNull final Audit audit,
      @NonNull final List<String> acceptHeaders,
      @NonNull final List<String> authHeaders) {

    getAccessToken()
        .andThenTry(auditAccessToken ->
            auditClient.createAudit(
                new String(jsonApiConverter.buildResourceConverter().writeDocument(
                    new JSONAPIDocument<>(audit))),
                String.join(",", acceptHeaders),
                String.join(",", authHeaders),
                "Bearer " + auditAccessToken.getAccessToken()))
        .onFailure(e -> {
          // Note the failure
          Log.error(GlobalConstants.MICROSERVICE_NAME + "-Audit-Failed", e);
          // As a fallback, write the audit event to the logs
          Try.run(() -> Log.error(OBJECT_MAPPER.writer().writeValueAsString(audit)));
        });
  }

  private Try<Oauth> getAccessToken() {
    return
        cognitoClientId.isPresent() && cognitoClientSecret.isPresent()
            ? Try.of(() -> cognitoClient.getToken(
            "Basic " + Base64.getEncoder()
                .encodeToString(
                    (cognitoClientId.get() + ":" + cognitoClientSecret.get()).getBytes()),
            GlobalConstants.CLIENT_CREDENTIALS,
            cognitoClientId.get(),
            GlobalConstants.AUDIT_SCOPE))
            : Try.failure(new Exception("Cognito client ID or secret were not set"));
  }

}
