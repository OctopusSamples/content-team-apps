package com.octopus.utilties;

import java.util.List;

/**
 * A service with methods to extract data partition information from headers.
 */
public interface PartitionIdentifier {

  /**
   * The "Data-Partition" header contains the partition information.
   *
   * @param header The "Data-Partition" header.
   * @param jwt    The JWT from the "Authorization" header.
   * @return The partition that the request is made under, defaulting to main.
   */
  String getPartition(List<String> header, String jwt);
}
