package com.octopus.lambda;

import java.util.List;
import java.util.Map;

/**
 * An interface used to extract query params from a Lambda request.
 */
public interface QueryParamExtractor {

  /**
   * Get all query params from both the multi and single query collections.
   *
   * @param multiQuery The collection holding multiple query param values.
   * @param query The collection holding single query param values.
   * @param name The name of the query param.
   * @return All the query param values that match the name.
   */
  List<String> getAllQueryParams(Map<String, List<String>> multiQuery, Map<String, String> query, String name);

  /**
   * Get all the query params from the multi query collection.
   *
   * @param query The collection holding multiple query param values.
   * @param name The name of the query param.
   * @return All the query param values that match the name.
   */
  List<String> getMultiQuery(Map<String, List<String>> query, String name);

  /**
   * Get the query param from the single query collection.
   *
   * @param query The collection holding single query param values.
   * @param name The name of the query param.
   * @return A list with zero or one query param values that match the name.
   */
  List<String> getQuery(Map<String, String> query, String name);
}
