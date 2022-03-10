package com.octopus.features;

import java.util.Optional;

/**
 * Simple wrapper around a property setting to aid with mocking in tests.
 */
public interface AdminJwtGroupFeature {
  Optional<String> getAdminGroup();
}
