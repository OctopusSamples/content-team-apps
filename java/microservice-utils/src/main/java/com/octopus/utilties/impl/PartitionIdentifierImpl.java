package com.octopus.utilties.impl;

import com.octopus.Constants;
import com.octopus.features.AdminJwtGroupFeature;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.jwt.JwtInspector;
import com.octopus.utilties.PartitionIdentifier;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 * A utility class with methods to extract data partition information from headers.
 */
public class PartitionIdentifierImpl implements PartitionIdentifier {

  private final JwtInspector jwtVerifier;
  private final AdminJwtGroupFeature adminJwtGroupFeature;
  private final DisableSecurityFeature cognitoDisableAuth;

  /**
   * Constructor.
   *
   * @param jwtVerifier          The JWT verifier service.
   * @param adminJwtGroupFeature The JWT admin group feature.
   * @param cognitoDisableAuth   The auth disable feature.
   */
  public PartitionIdentifierImpl(@NonNull final JwtInspector jwtVerifier,
      @NonNull final AdminJwtGroupFeature adminJwtGroupFeature,
      @NonNull final DisableSecurityFeature cognitoDisableAuth) {
    this.jwtVerifier = jwtVerifier;
    this.cognitoDisableAuth = cognitoDisableAuth;
    this.adminJwtGroupFeature = adminJwtGroupFeature;
  }

  /**
   * {@inheritDoc}
   */
  public String getPartition(final List<String> header, final String jwt) {
    /*
      The caller must be a member of a known group to make use of data partitions.
      Everyone else must work in the main partition.
     */
    if (!cognitoDisableAuth.getCognitoAuthDisabled()
        && (adminJwtGroupFeature.getAdminGroup().isEmpty()
        || StringUtils.isBlank(jwt)
        || !jwtVerifier.jwtContainsCognitoGroup(jwt, adminJwtGroupFeature.getAdminGroup().get()))) {
      return Constants.DEFAULT_PARTITION;
    }

    if (header == null || header.size() == 0 || header.stream().allMatch(StringUtils::isBlank)) {
      return Constants.DEFAULT_PARTITION;
    }

    return header.stream()
        // make sure we aren't processing null values
        .filter(Objects::nonNull)
        // split on commas for headers sent as a comma separated list
        .flatMap(h -> Stream.of(h.split(",")))
        // remove any blank strings
        .filter(s -> !StringUtils.isBlank(s))
        // trim all strings
        .map(String::trim)
        .findFirst()
        .orElse(Constants.DEFAULT_PARTITION);
  }
}
