package com.octopus.encryption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

public class CryptoUtilsTest {

  private static final AesCryptoUtils AES_CRYPTO_UTILS = new AesCryptoUtils();

  @Test
  public void testEncryptionAndDecryption() {
    for (int i = 0; i < 100; ++i) {
      final String generatedString = RandomStringUtils.random(32, true, true);
      final String password = RandomStringUtils.random(32, true, true);
      final String salt = RandomStringUtils.random(32, true, true);

      final String encrypted = AES_CRYPTO_UTILS.encrypt(generatedString, password, salt);
      final String decrypted = AES_CRYPTO_UTILS.decrypt(encrypted, password, salt);
      assertEquals(generatedString, decrypted);
      assertNotEquals(generatedString, encrypted);
    }
  }
}
