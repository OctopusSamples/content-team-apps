package com.octopus.audits.domain.utilities;

import com.octopus.audits.domain.Constants;
import com.octopus.audits.domain.utilities.impl.JoseJwtVerifier;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * A utility class with methods to extract data partition information from headers.
 */
@ApplicationScoped
public class PartitionIdentifier {

  @Inject
  JoseJwtVerifier jwtVerifier;

  @ConfigProperty(name = "cognito.admin-group")
  Optional<String> adminGroup;

  @ConfigProperty(name = "cognito.disable-auth")
  boolean cognitoDisableAuth;

  /**
   * The "Accept" header contains the version and partition information. For more information see
   * the discussion at https://github.com/json-api/json-api/issues/406.
   *
   * @param header The "Accept" header
   * @return The partition that the request is made under, defaulting to main.
   */
  public String getPartition(final List<String> header, final String jwt) {
    /*
      The caller must be a member of a known group to make use of data partitions.
      Everyone else must work in the main partition.
     */
    if (!cognitoDisableAuth
        && (adminGroup.isEmpty()
          || StringUtils.isEmpty(jwt)
          || !jwtVerifier.jwtContainsCognitoGroup(jwt, adminGroup.get()))) {
      return Constants.DEFAULT_PARTITION;
    }

    if (header == null || header.size() == 0 || header.stream().allMatch(StringUtils::isAllBlank)) {
      return Constants.DEFAULT_PARTITION;
    }

    return header.stream()
        // make sure we aren't processing null values
        .filter(Objects::nonNull)
        // split on commas for headers sent as a comma separated list
        .flatMap(h -> Stream.of(h.split(",")))
        // remove any blank strings
        .filter(s -> !StringUtils.isAllBlank(s))
        // trim all strings
        .map(String::trim)
        // We don't accept blank values
        .filter(a -> !StringUtils.isBlank(a))
        .findFirst()
        .orElse(Constants.DEFAULT_PARTITION);
  }
}
