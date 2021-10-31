package com.octopus.http;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class StringHttpClientTest {

  @Test
  public void testStringHttpClient() {
    final HttpClient httpClient = new StringHttpClient();
    assertTrue(httpClient.get("https://google.com").isSuccess());
    assertTrue(httpClient.head("https://google.com"));
  }

  @Test
  public void testStringHttpClientFailure() {
    final HttpClient httpClient = new StringHttpClient();
    assertFalse(httpClient.get("https://b171976a76324160a7fe21f45ba16de3.com").isSuccess());
    assertFalse(httpClient.head("https://b171976a76324160a7fe21f45ba16de3.com"));
  }
}
