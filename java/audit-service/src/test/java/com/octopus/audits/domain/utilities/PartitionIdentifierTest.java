package com.octopus.audits.domain.utilities;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.ArrayList;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.util.Assert;
import org.mockito.Mockito;

@QuarkusTest
public class PartitionIdentifierTest {

  @Inject
  PartitionIdentifier partitionIdentifier;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @BeforeEach
  public void setup() {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(true);
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

  @Test
  public void testBlankHeaders() {
    Assert.equals(
        "main",
        partitionIdentifier.getPartition(
            new ArrayList<>() {
              {
                add(" , ");
              }
            },
            "",
            true));
  }
}
