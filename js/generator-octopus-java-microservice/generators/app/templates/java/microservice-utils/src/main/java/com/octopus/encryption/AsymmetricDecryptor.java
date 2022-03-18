package com.octopus.encryption;

/**
 * An interface exposing string decryption methods.
 */
public interface AsymmetricDecryptor {

  /**
   * Decrypt a value.
   *
   * @param value           The value to encrypt.
   * @param publicKeyBase64 The password to encrypt the value with.
   * @return The decrypted value.
   */
  String decrypt(String value, String publicKeyBase64);
}
