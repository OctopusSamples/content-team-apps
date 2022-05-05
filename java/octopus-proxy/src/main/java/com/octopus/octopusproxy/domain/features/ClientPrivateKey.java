package com.octopus.octopusproxy.domain.features;

import io.smallrye.config.ConfigMapping;

/**
 * Captures the private key setting used to decrypt data sent from the client.
 */
@ConfigMapping(prefix = "client")
public interface ClientPrivateKey {

  /**
   * The private key base 64 encoded.
   *
   * @return The private key base 64 encoded.
   */
  String privateKeyBase64();
}
