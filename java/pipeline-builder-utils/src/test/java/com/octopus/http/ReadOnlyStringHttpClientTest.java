package com.octopus.http;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.http.impl.ReadOnlyHttpClientImpl;
import org.junit.jupiter.api.Test;

public class ReadOnlyStringHttpClientTest {

  @Test
  public void testStringHttpClient() {
    final ReadOnlyHttpClient readOnlyHttpClient = new ReadOnlyHttpClientImpl();
    assertTrue(readOnlyHttpClient.get("https://google.com").isSuccess());
    assertTrue(readOnlyHttpClient.head("https://google.com"));
  }

  @Test
  public void testStringHttpClientFailure() {
    final ReadOnlyHttpClient readOnlyHttpClient = new ReadOnlyHttpClientImpl();
    assertFalse(readOnlyHttpClient.get("https://b171976a76324160a7fe21f45ba16de3.com").isSuccess());
    assertFalse(readOnlyHttpClient.head("https://b171976a76324160a7fe21f45ba16de3.com"));
  }
}
