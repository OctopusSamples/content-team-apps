package com.octopus.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.impl.JoseJwtInspector;
import com.octopus.utilties.PartitionIdentifier;
import com.octopus.utilties.impl.PartitionIdentifierImpl;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class PartitionIdentifierTest {

  private static final PartitionIdentifier PARTITION_IDENTIFIER = new PartitionIdentifierImpl(
      new JoseJwtInspector(
          Optional::empty,
          () -> true,
          (jwt, jwk) -> false,
          () -> "test"
      ),
      Optional::empty,
      () -> true
  );

  private static final PartitionIdentifier PARTITION_IDENTIFIER_AUTH_ENABLED = new PartitionIdentifierImpl(
      new JwtInspector() {

        @Override
        public boolean jwtContainsCognitoGroup(String jwt, String group) {
          return true;
        }

        @Override
        public boolean jwtContainsScope(String jwt, String claim, String clientId) {
          return true;
        }

        @Override
        public Optional<String> getClaim(String jwt, String claim)  {
          return Optional.empty();
        }
      },
      () -> Optional.of("admin"),
      () -> false
  );

  private static final PartitionIdentifier PARTITION_IDENTIFIER_AUTH_ENABLED_JWT_FAILED = new PartitionIdentifierImpl(
      new JwtInspector() {

        @Override
        public boolean jwtContainsCognitoGroup(String jwt, String group) {
          return false;
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
      () -> Optional.of("admin"),
      () -> false
  );

  @Test
  public void testConstructorNulls() {
    assertThrows(NullPointerException.class, () -> new PartitionIdentifierImpl(
        null,
        Optional::empty,
        () -> true
    ));
    assertThrows(NullPointerException.class, () -> new PartitionIdentifierImpl(
        new JoseJwtInspector(
            Optional::empty,
            () -> true,
            (jwt, jwk) -> false,
            () -> "test"
        ),
        null,
        () -> true
    ));
    assertThrows(NullPointerException.class, () -> new PartitionIdentifierImpl(
        new JoseJwtInspector(
            Optional::empty,
            () -> true,
            (jwt, jwk) -> false,
            () -> "test"
        ),
        Optional::empty,
        null
    ));
  }

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
            ""));

    assertEquals(
        expected,
        PARTITION_IDENTIFIER_AUTH_ENABLED.getPartition(
            new ArrayList<>() {
              {
                add(dataPartitionHeader);
              }
            },
            "amockjwt"));
  }

  @ParameterizedTest
  @CsvSource({
      "main,main",
      "main,main ",
      "main, main ",
      "main,testing",
      "main,testing ",
      "main, testing ",
      "main, ",
      "main,"
  })
  public void testPartitionsEmptyJwt(final String expected, final String dataPartitionHeader) {
    assertEquals(
        expected,
        PARTITION_IDENTIFIER_AUTH_ENABLED.getPartition(
            new ArrayList<>() {
              {
                add(dataPartitionHeader);
              }
            },
            ""));
  }

  @ParameterizedTest
  @CsvSource({
      "main,main",
      "main,main ",
      "main, main ",
      "main,testing",
      "main,testing ",
      "main, testing ",
      "main, ",
      "main,"
  })
  public void testPartitionsJwtNotMatched(final String expected, final String dataPartitionHeader) {
    assertEquals(
        expected,
        PARTITION_IDENTIFIER_AUTH_ENABLED_JWT_FAILED.getPartition(
            new ArrayList<>() {
              {
                add(dataPartitionHeader);
              }
            },
            "mockjwt"));
  }

  @Test
  public void testBlankHeaders() {
    assertEquals(
        "main",
        PARTITION_IDENTIFIER.getPartition(
            new ArrayList<>() {
              {
                add(" , ");
              }
            },
            ""));
  }

  @Test
  public void testMissingHeaders() {
    assertEquals(
        "main",
        PARTITION_IDENTIFIER.getPartition(
            new ArrayList<>(),
            ""));
  }

  @Test
  public void testNullHeaders() {
    assertEquals(
        "main",
        PARTITION_IDENTIFIER.getPartition(
            null,
            ""));
  }

  @Test
  public void testEmptyHeaders() {
    assertEquals(
        "main",
        PARTITION_IDENTIFIER.getPartition(
            new ArrayList<>() {{
              add("");
            }},
            ""));
  }
}
