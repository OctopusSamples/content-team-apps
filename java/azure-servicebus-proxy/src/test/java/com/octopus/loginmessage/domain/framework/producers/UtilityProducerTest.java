package com.octopus.loginmessage.domain.framework.producers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.octopus.json.JsonSerializer;
import com.octopus.jsonapi.AcceptHeaderVerifier;
import com.octopus.jsonapi.PagedResultsLinksBuilder;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import com.octopus.jwt.JwtValidator;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import com.octopus.lambda.ProxyResponseBuilder;
import com.octopus.loginmessage.CommercialAzureServiceBusTestProfile;
import com.octopus.utilties.PartitionIdentifier;
import com.octopus.utilties.RegExUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * This test ensures the various services used by this microservice are available to be injected.
 */
@QuarkusTest
@TestProfile(CommercialAzureServiceBusTestProfile.class)
public class UtilityProducerTest {

  @Inject
  LambdaHttpValueExtractor lambdaHttpValueExtractor;

  @Inject
  LambdaHttpHeaderExtractor lambdaHttpHeaderExtractor;

  @Inject
  AcceptHeaderVerifier acceptHeaderVerifier;

  @Inject
  ProxyResponseBuilder proxyResponseBuilder;

  @Inject
  RegExUtils regExUtils;

  @Inject
  PagedResultsLinksBuilder pagedResultsLinksBuilder;

  @Inject
  JwtValidator jwtValidator;

  @Inject
  JwtUtils jwtUtils;

  @Inject
  JwtInspector jwtInspector;

  @Inject
  PartitionIdentifier partitionIdentifier;

  @Inject
  ServiceBusSenderClient serviceBusSenderClient;

  @Inject
  JsonSerializer jsonSerializer;

  @Test
  public void injectionsShouldNotBeNull() {
    assertNotNull(lambdaHttpValueExtractor);
    assertNotNull(lambdaHttpHeaderExtractor);
    assertNotNull(acceptHeaderVerifier);
    assertNotNull(proxyResponseBuilder);
    assertNotNull(regExUtils);
    assertNotNull(pagedResultsLinksBuilder);
    assertNotNull(jwtValidator);
    assertNotNull(jwtUtils);
    assertNotNull(jwtInspector);
    assertNotNull(partitionIdentifier);
    assertNotNull(serviceBusSenderClient);
    assertNotNull(jsonSerializer);

    // You need to call the injected objects for Quarkus to create the underlying objects.
    assertNotNull(lambdaHttpValueExtractor.toString());
    assertNotNull(lambdaHttpHeaderExtractor.toString());
    assertNotNull(acceptHeaderVerifier.toString());
    assertNotNull(proxyResponseBuilder.toString());
    assertNotNull(regExUtils.toString());
    assertNotNull(pagedResultsLinksBuilder.toString());
    assertNotNull(jwtValidator.toString());
    assertNotNull(jwtUtils.toString());
    assertNotNull(jwtInspector.toString());
    assertNotNull(partitionIdentifier.toString());
    assertNotNull(serviceBusSenderClient.toString());
    assertNotNull(jsonSerializer.toString());
  }
}
