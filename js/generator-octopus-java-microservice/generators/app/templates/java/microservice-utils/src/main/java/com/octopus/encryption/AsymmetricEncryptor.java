package com.octopus.encryption;

/**
 * An interface exposing string encryption methods.
 */
public interface AsymmetricEncryptor {

  /**
   * Decrypt a value.
   *
   * @param value    The value to encrypt.
   * @param publicKeyBase64 The private to encrypt the value with.
   * @return The decrypted value.
   */
  String encrypt(String value, String publicKeyBase64);
}
