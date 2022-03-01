package com.octopus.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An interface used to extract cookies from a Lambda request.
 */
public interface LambdaHttpCookieExtractor {

  /**
   * Get a single value from both the multi and single collections.
   *
   * @param input The Lambda request inputs.
   * @param name The name of the value.
   * @return All the  values that match the name.
   */
  Optional<String> getCookieValue(APIGatewayProxyRequestEvent input, String name);

  /**
   * Get all values from both the multi and single collections.
   *
   * @param input The Lambda request inputs.
   * @param name The name of the value.
   * @return All the  values that match the name.
   */
  List<String> getAllCookieValues(APIGatewayProxyRequestEvent input, String name);

  /**
   * Get all values from both the multi and single collections.
   *
   * @param multiQuery The collection holding multiple values.
   * @param query The collection holding single values.
   * @param name The name of the value.
   * @return All the  values that match the name.
   */
  List<String> getAllCookieValues(Map<String, List<String>> multiQuery, Map<String, String> query, String name);

  /**
   * Get all the values from the multi query collection.
   *
   * @param query The collection holding multiple values.
   * @param name The name of the value.
   * @return All the values that match the name.
   */
  List<String> getMultiCookie(Map<String, List<String>> query, String name);

  /**
   * Get the value from the single query collection.
   *
   * @param query The collection holding single values.
   * @param name The name of the value.
   * @return A list with zero or one values that match the name.
   */
  List<String> getCookie(Map<String, String> query, String name);
}
