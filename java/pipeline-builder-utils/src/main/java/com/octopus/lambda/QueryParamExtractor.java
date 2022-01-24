package com.octopus.lambda;

import java.util.List;
import java.util.Map;

public interface QueryParamExtractor {
  List<String> getAllQueryParams(Map<String, List<String>> multiQuery, Map<String, String> query, String header);
  List<String> getMultiQuery(Map<String, List<String>> query, String header);
  List<String> getQuery(Map<String, String> query, String header);
}
