package com.octopus.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import java.util.regex.Pattern;


/**
 * A service used to determine if a Lambda HTTP request matches a path regex and HTTP method.
 */
public interface RequestMatcher {

  /**
   * Determines if the HTTP request matches the inputs.
   *
   * @param input  The Lambda request.
   * @param regex  The path regex to match the request to.
   * @param method The HTTP method to match the request to.
   * @return true if the request is a match, and false otherwse.
   */
  boolean requestIsMatch(APIGatewayProxyRequestEvent input, Pattern regex, String method);
}
