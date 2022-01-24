package com.octopus.encryption;

public interface CryptoUtils {

  /**
   * Encrypt a value.
   *
   * @param value    The value to encrypt.
   * @param password The password to encrypt the value with.
   * @param salt     The encryption salt.
   * @return The encrypted value.
   */
  byte[] encrypt(String value, String password, String salt);

  /**
   * Decrypt a value.
   *
   * @param value    The value to encrypt.
   * @param password The password to encrypt the value with.
   * @param salt     The encryption salt.
   * @return The decrypted value.
   */
  byte[] decrypt(String value, String password, String salt);
}
