package com.octopus.githubactions.domain.features;

import com.azure.core.credential.TokenCredential;
import java.util.Optional;

/**
 * A feature that generates azure credentials.
 */
public interface AzureServiceBus {

  /**
   * Generate the azure credentials required to access an external service.
   *
   * @return The Azure credentials for a given service, or empty if the credentials are not
   * available.
   */
  Optional<TokenCredential> getCredentials();

  /**
   * Get the credential namespace.
   *
   * @return The credential namespace, or empty if it is not defined.
   */
  Optional<String> getNamespace();

  /**
   * Get the credential namespace.
   *
   * @return The credential namespace, or empty if it is not defined.
   */
  Optional<String> getTopic();
}
