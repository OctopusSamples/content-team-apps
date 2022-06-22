package com.octopus.audits.domain.utilities;

import static org.mockito.ArgumentMatchers.any;

import com.octopus.audits.domain.features.impl.AdminJwtGroupFeature;
import com.octopus.audits.domain.features.impl.DisableSecurityFeature;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.ArrayList;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.util.Assert;
import org.mockito.Mockito;

@QuarkusTest
public class PartitionIdentifierWithEmptyGroupConfigTest {

  @Inject
  PartitionIdentifier partitionIdentifier;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @InjectMock
  JwtVerifier jwtVerifier;

  @InjectMock
  AdminJwtGroupFeature adminJwtGroupFeature;

  @BeforeEach
  public void setup() {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(false);
    Mockito.when(jwtVerifier.jwtContainsCognitoGroup(any(), any())).thenReturn(true);
    Mockito.when(adminJwtGroupFeature.getAdminGroup()).thenReturn(Optional.empty());
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
