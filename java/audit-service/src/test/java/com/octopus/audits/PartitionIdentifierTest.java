package com.octopus.audits;

import com.octopus.audits.domain.utilities.PartitionIdentifier;
import io.quarkus.test.junit.QuarkusTest;
import java.util.ArrayList;
import javax.inject.Inject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.util.Assert;

@QuarkusTest
public class PartitionIdentifierTest {
  @Inject
  PartitionIdentifier partitionIdentifier;

  @ParameterizedTest
  @CsvSource({
    "main,main",
    "main,main ",
    "main, main ",
    "testing,testing",
    "testing,testing ",
    "testing, testing ",
    "main, ",
    "main,"
  })
  public void testPartitions(final String expected, final String dataPartitionHeader) {
    Assert.equals(
        expected,
        partitionIdentifier.getPartition(
            new ArrayList<>() {
              {
                add(dataPartitionHeader);
              }
            },
            ""));
  }
}
