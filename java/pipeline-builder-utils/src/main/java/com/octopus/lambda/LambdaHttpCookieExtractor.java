package com.octopus.lambda;

import java.util.List;
import java.util.Map;

/**
 * An interface used to extract cookies from a Lambda request.
 */
public interface LambdaHttpCookieExtractor {

  /**
   * Get all values from both the multi and single collections.
   *
   * @param multiQuery The collection holding multiple values.
   * @param query The collection holding single values.
   * @param name The name of the value.
   * @return All the  values that match the name.
   */
  List<String> getAllQueryParams(Map<String, List<String>> multiQuery, Map<String, String> query, String name);

  /**
   * Get all the values from the multi query collection.
   *
   * @param query The collection holding multiple values.
   * @param name The name of the value.
   * @return All the values that match the name.
   */
  List<String> getMultiQuery(Map<String, List<String>> query, String name);

  /**
   * Get the value from the single query collection.
   *
   * @param query The collection holding single values.
   * @param name The name of the value.
   * @return A list with zero or one values that match the name.
   */
  List<String> getQuery(Map<String, String> query, String name);
}
