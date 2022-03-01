package com.octopus.customers.domain.features;

import com.octopus.features.AdminJwtClaimFeature;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AdminJwtClaimFeatureImpl implements AdminJwtClaimFeature {

  @ConfigProperty(name = "cognito.admin-claim")
  Optional<String> cognitoAdminClaim;

  @Override
  public Optional<String> getAdminClaim() {
    return cognitoAdminClaim;
  }
}
