package com.octopus.octopub;

import com.octopus.octopub.domain.utilities.PartitionIdentifier;
import java.util.ArrayList;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.util.Assert;

public class PartitionIdentifierTest {
  public static final PartitionIdentifier PARTITION_IDENTIFIER = new PartitionIdentifier();

  @ParameterizedTest
  @CsvSource({
    "main,application/vnd.api+json; dataPartition=main",
    "main,application/vnd.api+json; dataPartition=main ",
    "main,application/vnd.api+json; dataPartition= main ",
    "testing,application/vnd.api+json; dataPartition=testing",
    "testing,application/vnd.api+json; dataPartition=testing ",
    "testing,application/vnd.api+json; dataPartition= testing ",
    "main,application/vnd.api+json; dataPartition=",
    "main,application/vnd.api+json; dataPartition= ",
    "main,application/vnd.api+json; ",
    "main,application/vnd.api+json",
    "main, ",
    "main,"
  })
  public void testPartitions(final String expected, final String acceptHeader) {
    Assert.equals(
        expected,
        PARTITION_IDENTIFIER.getPartition(
            new ArrayList<>() {
              {
                add(acceptHeader);
              }
            }));
    Assert.equals(
        expected,
        PARTITION_IDENTIFIER.getPartition(
            new ArrayList<>() {
              {
                add("application/vnd.api+json," + acceptHeader);
              }
            }));
    Assert.equals(
        expected,
        PARTITION_IDENTIFIER.getPartition(
            new ArrayList<>() {
              {
                add(acceptHeader);
                add("application/vnd.api+json");
              }
            }));
  }
}
