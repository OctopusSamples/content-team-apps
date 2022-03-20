package com.octopus.encryption;

/**
 * An interface exposing string decryption methods.
 */
public interface AsymmetricDecryptor {

  /**
   * Decrypt a value.
   *
   * @param value            The value to encrypt.
   * @param privateKeyBase64 The private key to decrypt the value with.
   * @return The decrypted value.
   */
  String decrypt(String value, String privateKeyBase64);
}
