package com.octopus.xray;

import java.util.Optional;

/**
 * Defines a service that parses the components of an AWS XRay header.
 */
public interface AwsXrayParser {

  /**
   * Returns the Self component.
   *
   * @param xray The XRay header.
   * @return The Self component.
   */
  Optional<String> getSelf(String xray);

  /**
   * Returns the Root component.
   *
   * @param xray The XRay header.
   * @return The Self component.
   */
  Optional<String> getRoot(String xray);
}
