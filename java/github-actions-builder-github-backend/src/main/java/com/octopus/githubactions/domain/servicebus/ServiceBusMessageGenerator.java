package com.octopus.githubactions.domain.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.githubactions.GlobalConstants;
import com.octopus.githubactions.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.githubactions.domain.features.ServiceBusCognitoConfig;
import com.octopus.githubactions.domain.framework.jsonapi.JsonApiConverter;
import com.octopus.githubactions.infrastructure.client.CognitoClient;
import com.octopus.githubactions.infrastructure.client.ServiceBusProxyClient;
import io.quarkus.logging.Log;
import io.vavr.control.Try;
import java.util.Base64;
import java.util.Date;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * A service used to write login events to the Azure service bus proxy microservice.
 */
@ApplicationScoped
public class ServiceBusMessageGenerator {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static long expiry;
  private static String accessToken;

  @RestClient
  ServiceBusProxyClient serviceBusProxyClient;

  @RestClient
  CognitoClient cognitoClient;

  @Inject
  JsonApiConverter jsonApiConverter;

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  @Inject
  ServiceBusCognitoConfig serviceBusCognitoConfig;

  /**
   * Create an audit event.
   *
   * @param loginMessage         The details of the login event.
   * @param routingHeaders       The "routing" headers to propagate with the request.
   * @param dataPartitionHeaders The "data-partition" headers to propagate with the request.
   * @param authHeaders          The "authorization" headers propagate with the request.
   */
  public void sendLoginMessage(
      @NonNull final GithubUserLoggedInForFreeToolsEventV1 loginMessage,
      final String xrayId,
      @NonNull final String routingHeaders,
      @NonNull final String dataPartitionHeaders,
      @NonNull final String authHeaders) {

    getAccessToken()
        .andThenTry(auditAccessToken ->
            serviceBusProxyClient.createLoginMessage(
                new String(jsonApiConverter.buildResourceConverter().writeDocument(
                    new JSONAPIDocument<>(loginMessage))),
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
          Try.run(() -> Log.error(OBJECT_MAPPER.writer().writeValueAsString(loginMessage)));
        });
  }

  private Try<String> getAccessToken() {
    if (!StringUtils.isEmpty(accessToken) && new Date().getTime() < expiry) {
      return Try.of(() -> accessToken);
    }

    if (serviceBusCognitoConfig.clientId().isPresent() && serviceBusCognitoConfig.clientSecret().isPresent()) {
      return Try.of(() -> cognitoClient.getToken(
              "Basic " + Base64.getEncoder()
                  .encodeToString(
                      (serviceBusCognitoConfig.clientId().get() + ":" + serviceBusCognitoConfig.clientSecret().get()).getBytes()),
              GlobalConstants.CLIENT_CREDENTIALS,
              serviceBusCognitoConfig.clientId().get(),
              GlobalConstants.LOGINMESSAGE_SCOPE))
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
