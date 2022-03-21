package com.octopus.loginmessage.domain.servicebus.impl;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.octopus.loginmessage.domain.features.ServiceBusConfig;
import com.octopus.loginmessage.domain.servicebus.AzureServiceBus;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;

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
  public TokenCredential getCredentials() {
    return new ClientSecretCredentialBuilder()
        .clientId(serviceBusConfig.appId())
        .clientSecret(serviceBusConfig.secret())
        .tenantId(serviceBusConfig.tenant())
        .build();
  }

  @Override
  public String getNamespace() {
    return serviceBusConfig.namespace();
  }

  public String getTopic() {
    return serviceBusConfig.topic();
  }
}
