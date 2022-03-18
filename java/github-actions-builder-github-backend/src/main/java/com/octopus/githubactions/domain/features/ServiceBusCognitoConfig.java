package com.octopus.githubactions.domain.features;

import io.smallrye.config.ConfigMapping;
import java.util.Optional;

/**
 * Groups together the azure message bus config settings.
 */
@ConfigMapping(prefix = "cognito.servicebus")
public interface ServiceBusCognitoConfig {

  /**
   * The cognito client id.
   *
   * @return The cognito client id..
   */
  Optional<String> clientId();

  /**
   * The cognito client secret.
   *
   * @return The cognito client secret.
   */
  Optional<String> clientSecret();

}
