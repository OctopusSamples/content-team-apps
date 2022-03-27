package com.octopus.xray.impl;

import com.octopus.xray.AwsXrayParser;
import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * An implementation of AwsXrayParser that splits XRay headers into their individual components.
 */
public class AwsXrayParserImpl implements AwsXrayParser {

  @Override
  public Optional<String> getSelf(final String xray) {
    return Arrays.stream(StringUtils.defaultString(xray, "").trim()
            .split(";"))
        .filter(x -> x.startsWith("Self"))
        .map(x -> x.split("="))
        .filter(x -> x.length == 2)
        .map(x -> x[1])
        .findFirst();
  }

  @Override
  public Optional<String> getRoot(final String xray) {
    return Arrays.stream(StringUtils.defaultString(xray, "").trim()
            .split(";"))
        .filter(x -> x.startsWith("Root"))
        .map(x -> x.split("="))
        .filter(x -> x.length == 2)
        .map(x -> x[1])
        .findFirst();
  }
}
