package com.octopus.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An interface used to extract values from a Lambda request.
 */
public interface LambdaHttpHeaderExtractor {

  /**
   * Get a single value from both the multi and single collections.
   *
   * @param input The Lambda request inputs.
   * @param name The name of the value.
   * @return All the values that match the name.
   */
  Optional<String> getFirstHeader(APIGatewayProxyRequestEvent input, String name);

  /**
   * Get all values from both the multi and single collections.
   *
   * @param input The Lambda request inputs.
   * @param name The name of the value.
   * @return All the  values that match the name.
   */
  List<String> getAllHeaders(APIGatewayProxyRequestEvent input, String name);

  /**
   * Get all values from both the multi and single collections.
   *
   * @param multiHeader The collection holding multiple values.
   * @param header The collection holding single values.
   * @param name The name of the value.
   * @return All the  values that match the name.
   */
  List<String> getAllHeaders(Map<String, List<String>> multiHeader, Map<String, String> header, String name);

  /**
   * Get all the values from the multi Header collection.
   *
   * @param header The collection holding multiple values.
   * @param name The name of the value.
   * @return All the values that match the name.
   */
  List<String> getMultiHeader(Map<String, List<String>> header, String name);

  /**
   * Get the value from the single Header collection.
   *
   * @param header The collection holding single values.
   * @param name The name of the value.
   * @return A list with zero or one values that match the name.
   */
  List<String> getHeader(Map<String, String> header, String name);
}
