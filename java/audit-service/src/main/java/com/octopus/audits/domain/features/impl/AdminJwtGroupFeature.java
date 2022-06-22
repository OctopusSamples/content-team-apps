package com.octopus.audits.domain.features.impl;

import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Simple wrapper around a property setting to aid with mocking in tests.
 */
@ApplicationScoped
public class AdminJwtGroupFeature {
  @ConfigProperty(name = "cognito.admin-group")
  Optional<String> adminGroup;

  public  Optional<String> getAdminGroup() {
    return adminGroup;
  }
}
