package com.octopus.lambda.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.octopus.lambda.RequestMatcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;

/**
 * An implementation of RequestMatcher.
 */
public class RequestMatcherImpl implements RequestMatcher {

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean requestIsMatch(
      @NonNull final APIGatewayProxyRequestEvent input,
      @NonNull final Pattern regex,
      @NonNull final String method) {
    final String path = ObjectUtils.defaultIfNull(input.getPath(), "");
    final String requestMethod = ObjectUtils.defaultIfNull(input.getHttpMethod(), "").toLowerCase();
    return regex.matcher(path).matches() && method.toLowerCase().equals(requestMethod);
  }
}
