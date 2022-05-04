package com.octopus.loginmessage.infrastructure.octofront.impl;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.google.common.base.Preconditions;
import com.octopus.loginmessage.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.loginmessage.domain.features.DisableServiceBus;
import com.octopus.loginmessage.domain.features.UpstreamServiceName;
import com.octopus.loginmessage.infrastructure.octofront.CommercialServiceBus;
import com.octopus.features.MicroserviceNameFeature;
import io.quarkus.logging.Log;
import java.time.Instant;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 * A service that sends messages to the commercial team service bus.
 */
@ApplicationScoped
public class CommercialServiceBusImpl implements CommercialServiceBus {

  private static final String SOURCE_KEY = "source";
  private static final String CONTEXT_KEY = "context";
  private static final String TYPE_KEY = "type";
  private static final String OCCURRED_TIME_UTC_KEY = "occurredTimeUtc";
  private static final String ACTIVITY_ID_KEY = "activityId";
  private static final String SPEC_VERSION_KEY = "specVersion";
  private static final String CONTEXT = "marketing";
  private static final String SPEC_VERSION = "1";

  @Inject
  ServiceBusSenderClient serviceBusSenderClient;

  @Inject
  DisableServiceBus disableServiceBus;

  @Inject
  UpstreamServiceName upstreamServiceName;

  @Override
  public void sendUserDetails(final String traceId, @NonNull final String body) {
    Preconditions.checkArgument(StringUtils.isNotBlank(body), "body can not be blank");

    if (!disableServiceBus.disabled()) {
      serviceBusSenderClient.sendMessage(generateMessage(traceId, body));
      Log.info("Successfully sent message to service bus");
    }
  }

  ServiceBusMessage generateMessage(final String traceId, final String body) {
    final ServiceBusMessage message = new ServiceBusMessage(body);
    message.setMessageId(UUID.randomUUID().toString());
    message.setCorrelationId(traceId);
    message.setContentType("application/json");
    message.getApplicationProperties()
        .put(SOURCE_KEY, upstreamServiceName.upstreamServiceName());
    message.getApplicationProperties().put(CONTEXT_KEY, CONTEXT);
    message.getApplicationProperties().put(TYPE_KEY, GithubUserLoggedInForFreeToolsEventV1.class.getSimpleName());
    message.getApplicationProperties().put(OCCURRED_TIME_UTC_KEY, Instant.now().toString());
    message.getApplicationProperties().put(ACTIVITY_ID_KEY, traceId);
    message.getApplicationProperties().put(SPEC_VERSION_KEY, SPEC_VERSION);
    return message;
  }
}
