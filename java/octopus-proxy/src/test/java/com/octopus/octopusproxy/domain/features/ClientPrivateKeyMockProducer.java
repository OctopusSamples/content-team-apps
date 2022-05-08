package com.octopus.octopusproxy.domain.features;

import io.smallrye.config.SmallRyeConfig;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Produces;
import org.eclipse.microprofile.config.Config;

/**
 * https://quarkus.io/guides/config-mappings#mocking.
 */
public class ClientPrivateKeyMockProducer {
  @Inject
  Config config;

  @Produces
  @ApplicationScoped
  @io.quarkus.test.Mock
  ClientPrivateKey clientPrivateKey() {
    return config.unwrap(SmallRyeConfig.class).getConfigMapping(ClientPrivateKey.class);
  }
}
