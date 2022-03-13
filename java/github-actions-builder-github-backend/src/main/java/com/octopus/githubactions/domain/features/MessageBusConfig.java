package com.octopus.githubactions.domain.features;

import io.smallrye.config.ConfigMapping;
import java.util.Optional;

/**
 * Groups together the azure message bus config settings.
 */
@ConfigMapping(prefix = "commercial.messagebus")
public interface MessageBusConfig {
  Optional<String> topic();
  Optional<String> namespace();
  Optional<String> secret();
  Optional<String> tenant();
  Optional<String> appId();
}
