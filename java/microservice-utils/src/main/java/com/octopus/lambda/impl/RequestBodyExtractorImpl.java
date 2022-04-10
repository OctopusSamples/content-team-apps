package com.octopus.lambda.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.octopus.lambda.RequestBodyExtractor;
import java.util.Base64;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;

/**
 * An implementation of RequestBodyExtractor.
 */
public class RequestBodyExtractorImpl implements RequestBodyExtractor {

  /**
   * {@inheritDoc}
   */
  @Override
  public String getBody(@NonNull final APIGatewayProxyRequestEvent input) {
    final String body = ObjectUtils.defaultIfNull(input.getBody(), "");
    final String isBase64Encoded =
        ObjectUtils.defaultIfNull(input.getIsBase64Encoded(), "").toString().toLowerCase();

    if ("true".equals(isBase64Encoded)) {
      return new String(Base64.getDecoder().decode(body));
    }

    return body;
  }
}
