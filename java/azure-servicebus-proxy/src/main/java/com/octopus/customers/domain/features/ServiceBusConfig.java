package com.octopus.customers.domain.features;

import io.smallrye.config.ConfigMapping;
import java.util.Optional;

/**
 * Groups together the azure message bus config settings.
 */
@ConfigMapping(prefix = "commercial.servicebus")
public interface ServiceBusConfig {

  /**
   * The service bus topic to send messages to.
   *
   * @return The service bus topic.
   */
  Optional<String> topic();

  /**
   * The service bus topic namespace.
   *
   * @return The service bus namespace.
   */
  Optional<String> namespace();

  /**
   * The service bus topic auth secret.
   *
   * @return The service bus auth secret.
   */
  Optional<String> secret();

  /**
   * The service bus topic auth tenant.
   *
   * @return The service bus auth tenant.
   */
  Optional<String> tenant();

  /**
   * The service bus topic auth application id.
   *
   * @return The service bus auth application id.
   */
  Optional<String> appId();
}
