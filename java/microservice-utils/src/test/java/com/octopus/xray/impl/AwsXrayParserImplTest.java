package com.octopus.xray.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class AwsXrayParserImplTest {
  private static final AwsXrayParserImpl AWS_XRAY_PARSER = new AwsXrayParserImpl();

  @Test
  public void testGetSelf() {
    assertEquals(
        "1-62382f80-7bb0215908d69e92762eda43",
        AWS_XRAY_PARSER.getSelf("Self=1-62382f80-7bb0215908d69e92762eda43;Root=1-62382f7f-11fd455f1b8a116b3047ae73").get());
    assertEquals(
        "1-62382f80-7bb0215908d69e92762eda43",
        AWS_XRAY_PARSER.getSelf(" Self=1-62382f80-7bb0215908d69e92762eda43;Root=1-62382f7f-11fd455f1b8a116b3047ae73").get());
    assertEquals(
        "1-62382f80-7bb0215908d69e92762eda43",
        AWS_XRAY_PARSER.getSelf("Self=1-62382f80-7bb0215908d69e92762eda43;Root=1-62382f7f-11fd455f1b8a116b3047ae73 ").get());
    assertEquals(
        "1-62382f80-7bb0215908d69e92762eda43",
        AWS_XRAY_PARSER.getSelf(" Self=1-62382f80-7bb0215908d69e92762eda43;Root=1-62382f7f-11fd455f1b8a116b3047ae73 ").get());
    assertTrue(AWS_XRAY_PARSER.getSelf("").isEmpty());
    assertTrue(AWS_XRAY_PARSER.getSelf(null).isEmpty());
    assertTrue(AWS_XRAY_PARSER.getSelf("blah").isEmpty());
  }

  @Test
  public void testGetRoot() {
    assertEquals(
        "1-62382f7f-11fd455f1b8a116b3047ae73",
        AWS_XRAY_PARSER.getRoot("Self=1-62382f80-7bb0215908d69e92762eda43;Root=1-62382f7f-11fd455f1b8a116b3047ae73").get());
    assertEquals(
        "1-62382f7f-11fd455f1b8a116b3047ae73",
        AWS_XRAY_PARSER.getRoot(" Self=1-62382f80-7bb0215908d69e92762eda43;Root=1-62382f7f-11fd455f1b8a116b3047ae73").get());
    assertEquals(
        "1-62382f7f-11fd455f1b8a116b3047ae73",
        AWS_XRAY_PARSER.getRoot("Self=1-62382f80-7bb0215908d69e92762eda43;Root=1-62382f7f-11fd455f1b8a116b3047ae73 ").get());
    assertEquals(
        "1-62382f7f-11fd455f1b8a116b3047ae73",
        AWS_XRAY_PARSER.getRoot(" Self=1-62382f80-7bb0215908d69e92762eda43;Root=1-62382f7f-11fd455f1b8a116b3047ae73 ").get());
    assertTrue(AWS_XRAY_PARSER.getRoot("").isEmpty());
    assertTrue(AWS_XRAY_PARSER.getRoot(null).isEmpty());
    assertTrue(AWS_XRAY_PARSER.getRoot("blah").isEmpty());
  }
}
