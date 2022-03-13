package com.octopus.githubactions.domain.servicebus.impl;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.octopus.githubactions.domain.servicebus.AzureServiceBus;
import com.octopus.githubactions.domain.features.MessageBusConfig;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * A feature that builds the azure credentials required to post user details to the commercial
 * team's message bus.
 */
@Named("CommercialMessageBus")
@ApplicationScoped
public class CommercialAzureServiceBus implements AzureServiceBus {

  @Inject
  MessageBusConfig messageBusConfig;

  @Override
  public Optional<TokenCredential> getCredentials() {
    if (messageBusConfig.appId().isEmpty() || messageBusConfig.secret().isEmpty() || messageBusConfig.tenant().isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new ClientSecretCredentialBuilder()
        .clientId(messageBusConfig.appId().get())
        .clientSecret(messageBusConfig.secret().get())
        .tenantId(messageBusConfig.tenant().get())
        .build());
  }

  @Override
  public Optional<String> getNamespace() {
    return messageBusConfig.namespace();
  }

  public Optional<String> getTopic() {
    return messageBusConfig.topic();
  }
}
