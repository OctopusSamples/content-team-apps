package com.octopus.encryption;

/**
 * An interface exposing string encryption and decryption methods.
 */
public interface CryptoUtils {

  /**
   * Encrypt a value.
   *
   * @param value    The value to encrypt.
   * @param password The password to encrypt the value with.
   * @param salt     The encryption salt.
   * @return The encrypted value.
   */
  String encrypt(String value, String password, String salt);

  /**
   * Decrypt a value.
   *
   * @param value    The value to encrypt.
   * @param password The password to encrypt the value with.
   * @param salt     The encryption salt.
   * @return The decrypted value.
   */
  String decrypt(String value, String password, String salt);
}
