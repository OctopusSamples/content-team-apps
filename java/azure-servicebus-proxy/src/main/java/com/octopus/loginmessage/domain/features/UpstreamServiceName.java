package com.octopus.loginmessage.domain.features;

import io.smallrye.config.ConfigMapping;

/**
 * Defines the upstream service name.
 */
@ConfigMapping(prefix = "commercial.servicebus")
public interface UpstreamServiceName {

  /**
   * The name of the upstream service is sent to the Azure service bus to track statistics. This
   * should be unique for each instance of the proxy.
   *
   * @return The name of the upstream service.
   */
  String upstreamServiceName();
}
