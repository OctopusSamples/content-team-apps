package com.octopus.http.impl;

import com.octopus.http.FormBodyParser;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class FormBodyParserImpl implements FormBodyParser {

  @Override
  public Map<String, String> parseFormBody(final String body) {
    if (StringUtils.isBlank(body)) {
      return new HashMap<>();
    }

    return Arrays.stream(body.split("&"))
        .map(a -> a.split("="))
        .collect(
            Collectors.toMap(a -> a[0], a -> a.length == 2 ? a[1] : ""));
  }
}
