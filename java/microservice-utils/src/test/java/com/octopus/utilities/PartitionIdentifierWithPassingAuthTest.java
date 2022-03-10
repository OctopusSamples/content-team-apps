package com.octopus.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.octopus.jwt.JwtInspector;
import com.octopus.utilties.PartitionIdentifier;
import com.octopus.utilties.impl.PartitionIdentifierImpl;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class PartitionIdentifierWithPassingAuthTest {

  private static final PartitionIdentifier PARTITION_IDENTIFIER = new PartitionIdentifierImpl(
      new JwtInspector() {
        @Override
        public boolean jwtContainsCognitoGroup(String jwt, String group) {
          return true;
        }

        @Override
        public boolean jwtContainsScope(String jwt, String claim, String clientId) {
          return false;
        }

        @Override
        public Optional<String> getClaim(String jwt, String claim)  {
          return Optional.empty();
        }
      },
      () -> Optional.of("Developers"),
      () -> false
  );

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
    assertEquals(
        expected,
        PARTITION_IDENTIFIER.getPartition(
            new ArrayList<>() {
              {
                add(dataPartitionHeader);
              }
            },
            "blah"));
  }
}
