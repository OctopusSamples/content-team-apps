package com.octopus.features;

/**
 * Simple wrapper around a property defining the name of the microservice to allow different
 * frameworks to supply the value using their own canonical configuration system.
 */
public interface MicroserviceNameFeature {
  String getMicroserviceName();
}
