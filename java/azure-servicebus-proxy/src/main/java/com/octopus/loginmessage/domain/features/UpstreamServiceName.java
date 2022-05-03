package com.octopus.loginmessage.domain.features;

import io.smallrye.config.ConfigMapping;

/**
 * Defines the upstream service name. This is sent to the Azure service bus.
 */
@ConfigMapping(prefix = "commercial.servicebus")
public interface UpstreamServiceName {

  /**
   * @return The name of the upstream service.
   */
  String upstreamServiceName();
}
