package com.octopus.loginmessage.domain.features;

import io.smallrye.config.ConfigMapping;

/**
 * A feature that determines if the upstream service bus is disabled. Mostly just used for testing.
 */
@ConfigMapping(prefix = "commercial.servicebus")
public interface DisableServiceBus {
  boolean disabled();
}
