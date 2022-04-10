package com.octopus.lambda.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.octopus.lambda.RequestBodyExtractor;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

public class RequestBodyExtractorImplTest {

  private static final RequestBodyExtractor REQUEST_BODY_EXTRACTOR = new RequestBodyExtractorImpl();

  @Test
  public void testRequestBodyExtractor() {
    assertEquals("body", REQUEST_BODY_EXTRACTOR.getBody(
        new APIGatewayProxyRequestEvent().withBody("body").withIsBase64Encoded(false)));

    assertEquals("body", REQUEST_BODY_EXTRACTOR.getBody(
        new APIGatewayProxyRequestEvent().withBody(Base64.getEncoder().encodeToString("body".getBytes(StandardCharsets.UTF_8)))
            .withIsBase64Encoded(true)));
  }

  @Test
  public void testNullParams() {
    assertThrows(NullPointerException.class, () -> {
      REQUEST_BODY_EXTRACTOR.getBody(null);
    });
  }
}
