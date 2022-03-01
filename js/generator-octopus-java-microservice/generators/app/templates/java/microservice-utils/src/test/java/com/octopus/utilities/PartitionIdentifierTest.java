package com.octopus.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
