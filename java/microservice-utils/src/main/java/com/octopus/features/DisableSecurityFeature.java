package com.octopus.features;

/**
 * Simple wrapper around a property setting to aid with mocking in tests and to allow different
 * frameworks to supply the value using their own canonical configuration system.
 */
public interface DisableSecurityFeature {
  boolean getCognitoAuthDisabled();
}
