package com.octopus.githubactions.domain.servicebus.impl;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.octopus.githubactions.domain.servicebus.AzureServiceBus;
import com.octopus.githubactions.domain.features.ServiceBusConfig;
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
  ServiceBusConfig serviceBusConfig;

  @Override
  public Optional<TokenCredential> getCredentials() {
    if (serviceBusConfig.appId().isEmpty() || serviceBusConfig.secret().isEmpty() || serviceBusConfig.tenant().isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new ClientSecretCredentialBuilder()
        .clientId(serviceBusConfig.appId().get())
        .clientSecret(serviceBusConfig.secret().get())
        .tenantId(serviceBusConfig.tenant().get())
        .build());
  }

  @Override
  public Optional<String> getNamespace() {
    return serviceBusConfig.namespace();
  }

  public Optional<String> getTopic() {
    return serviceBusConfig.topic();
  }
}
