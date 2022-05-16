package com.octopus.products.domain.features.impl;

import com.octopus.features.AdminJwtClaimFeature;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * An implementation of AdminJwtClaimFeature that exposes the admin claim from the properties file.
 */
@ApplicationScoped
public class AdminJwtClaimFeatureImpl implements AdminJwtClaimFeature {

  @ConfigProperty(name = "cognito.admin-claim")
  Optional<String> cognitoAdminClaim;

  @Override
  public Optional<String> getAdminClaim() {
    return cognitoAdminClaim;
  }
}
