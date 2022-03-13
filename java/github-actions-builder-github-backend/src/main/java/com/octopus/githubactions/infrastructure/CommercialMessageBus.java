package com.octopus.githubactions.infrastructure;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.octopus.features.MicroserviceNameFeature;
import io.quarkus.logging.Log;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CommercialMessageBus {

  private static final String SOURCE_KEY = "source";
  private static final String CONTEXT_KEY = "context";
  private static final String TYPE_KEY = "type";
  private static final String OCCURRED_TIME_UTC_KEY = "occurredTimeUtc";
  private static final String ACTIVITY_ID_KEY = "activityId";
  private static final String SPEC_VERSION_KEY = "specVersion";
  private static final String CONTEXT = "marketing";
  private static final String TYPE = "UserSignIn";
  private static final String SPEC_VERSION = "1";

  @Inject
  Optional<ServiceBusSenderClient> serviceBusSenderClient;

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  public void sendUserDetails(final String traceId, final String body) {
    if (serviceBusSenderClient.isEmpty()) {
      Log.error(microserviceNameFeature.getMicroserviceName()
          + """
          -DataCollection-AzureMessageBusNotConfigured The Azure service bus credentials or queue
          are not available, so user details will not be shared with commercial team. To resolve 
          this issue, ensure the appropriate commercial.messagebus properties are defined in the
          application.properties file.
          """
      );
      return;
    }

    final ServiceBusMessage message = new ServiceBusMessage(body);
    message.setMessageId(UUID.randomUUID().toString());
    message.setCorrelationId(traceId);
    message.setContentType("application/json");
    message.getApplicationProperties()
        .put(SOURCE_KEY, microserviceNameFeature.getMicroserviceName());
    message.getApplicationProperties().put(CONTEXT_KEY, CONTEXT);
    message.getApplicationProperties().put(TYPE_KEY, TYPE);
    message.getApplicationProperties().put(OCCURRED_TIME_UTC_KEY, Instant.now().toString());
    message.getApplicationProperties().put(ACTIVITY_ID_KEY, traceId);
    message.getApplicationProperties().put(SPEC_VERSION_KEY, SPEC_VERSION);

    serviceBusSenderClient.get().sendMessage(message);
  }
}
