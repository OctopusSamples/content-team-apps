package com.octopus.encryption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.octopus.encryption.impl.AesCryptoUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
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

  @Test
  public void testKeyAndSaltLengths() {
    final String generatedString = RandomStringUtils.random(10, true, true);
    final String shortPassword = RandomStringUtils.random(10, true, true);
    final String shortSalt = RandomStringUtils.random(10, true, true);
    final String password = RandomStringUtils.random(32, true, true);
    final String salt = RandomStringUtils.random(32, true, true);

    Assertions.assertThrows(IllegalArgumentException.class, () -> AES_CRYPTO_UTILS.encrypt(generatedString, shortPassword, salt));
    Assertions.assertThrows(IllegalArgumentException.class, () -> AES_CRYPTO_UTILS.encrypt(generatedString, "", salt));
    Assertions.assertThrows(NullPointerException.class, () -> AES_CRYPTO_UTILS.encrypt(generatedString, null, salt));
    Assertions.assertThrows(IllegalArgumentException.class, () -> AES_CRYPTO_UTILS.encrypt(generatedString, password, shortSalt));
    Assertions.assertThrows(IllegalArgumentException.class, () -> AES_CRYPTO_UTILS.encrypt(generatedString, password, ""));
    Assertions.assertThrows(NullPointerException.class, () -> AES_CRYPTO_UTILS.encrypt(generatedString, password, null));
    Assertions.assertThrows(NullPointerException.class, () -> AES_CRYPTO_UTILS.encrypt(null, password, salt));

    Assertions.assertThrows(IllegalArgumentException.class, () -> AES_CRYPTO_UTILS.decrypt(generatedString, "", salt));
    Assertions.assertThrows(NullPointerException.class, () -> AES_CRYPTO_UTILS.decrypt(generatedString, null, salt));
    Assertions.assertThrows(IllegalArgumentException.class, () -> AES_CRYPTO_UTILS.decrypt(generatedString, password, ""));
    Assertions.assertThrows(NullPointerException.class, () -> AES_CRYPTO_UTILS.decrypt(generatedString, password, null));
    Assertions.assertThrows(NullPointerException.class, () -> AES_CRYPTO_UTILS.decrypt(null, password, salt));
    Assertions.assertThrows(IllegalArgumentException.class, () -> AES_CRYPTO_UTILS.decrypt(" ", password, salt));
  }
}
