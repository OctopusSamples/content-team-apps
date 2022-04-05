package com.octopus.jenkins.github.domain.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.octopus.Constants;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.jenkins.github.GlobalConstants;
import com.octopus.jenkins.github.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.jenkins.github.domain.framework.jsonapi.JsonApiConverter;
import com.octopus.jenkins.github.infrastructure.client.ServiceBusProxyClient;
import com.octopus.oauth.OauthClientCredsAccessor;
import com.octopus.utilties.PartitionIdentifier;
import io.quarkus.logging.Log;
import io.vavr.control.Try;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * A service used to write login events to the Azure service bus proxy microservice.
 */
@ApplicationScoped
public class ServiceBusMessageGenerator {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Inject
  OauthClientCredsAccessor oauthClientCredsAccessor;

  @RestClient
  ServiceBusProxyClient serviceBusProxyClient;

  @Inject
  JsonApiConverter jsonApiConverter;

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  @Inject
  PartitionIdentifier partitionIdentifier;

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

    // Any testing in another data partition won't be recorded in upstream services
    if (!Constants.DEFAULT_PARTITION.equals(partitionIdentifier.getPartition(List.of(dataPartitionHeaders), authHeaders))) {
      return;
    }

    oauthClientCredsAccessor.getAccessToken(GlobalConstants.LOGINMESSAGE_SCOPE)
        .andThenTry(auditAccessToken -> {
          final Response response = serviceBusProxyClient.createLoginMessage(
              new String(jsonApiConverter.buildResourceConverter().writeDocument(
                  new JSONAPIDocument<>(loginMessage))),
              StringUtils.defaultString(xrayId),
              routingHeaders,
              dataPartitionHeaders,
              authHeaders,
              "Bearer " + auditAccessToken,
              GlobalConstants.ASYNC_INVOCATION_TYPE);
          if (response.getStatus() != 202 && response.getStatus() != 200) {
            throw new RuntimeException();
          }
        })
        .onFailure(e -> {
          // Note the failure
          Log.error(microserviceNameFeature.getMicroserviceName() + "-ServiceBusMessage-Failed", e);
          // As a fallback, write the audit event to the logs
          Try.run(() -> Log.error(OBJECT_MAPPER.writer().writeValueAsString(loginMessage)));
        });
  }
}
