package com.octopus.audits.domain.utilities;

import static org.mockito.ArgumentMatchers.any;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.audits.domain.utilities.impl.JoseJwtVerifier;
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
public class PartitionIdentifierWithAuthTest {

  @Inject
  PartitionIdentifier partitionIdentifier;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @InjectMock
  JoseJwtVerifier jwtVerifier;

  @BeforeEach
  public void setup() {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(false);
    Mockito.when(jwtVerifier.jwtContainsCognitoGroup(any(), any())).thenReturn(false);
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
  public void testPartitions(final String expected, final String dataPartitionHeader) {
    Assert.equals(
        expected,
        partitionIdentifier.getPartition(
            new ArrayList<>() {
              {
                add(dataPartitionHeader);
              }
            },
            "blah"));
  }
}
