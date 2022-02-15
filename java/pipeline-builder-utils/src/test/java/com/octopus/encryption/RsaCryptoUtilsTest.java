package com.octopus.encryption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.common.io.Resources;
import com.octopus.encryption.impl.RsaCryptoUtilsDecryptor;
import com.octopus.encryption.impl.RsaCryptoUtilsEncryptor;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RsaCryptoUtilsTest {
  private RsaCryptoUtilsEncryptor rsaCryptoUtilsEncryptor;
  private RsaCryptoUtilsDecryptor rsaCryptoUtilsDecryptor;
  private String privateKeyBase64;
  private String publicKeyBase64;

  @BeforeEach
  public void init() throws NoSuchPaddingException, NoSuchAlgorithmException, IOException {
    rsaCryptoUtilsEncryptor = new RsaCryptoUtilsEncryptor();
    rsaCryptoUtilsDecryptor = new RsaCryptoUtilsDecryptor();
    privateKeyBase64 = Base64.getEncoder().encodeToString(Resources.toByteArray(Resources.getResource("keypair/private_key.der")));
    publicKeyBase64 = Base64.getEncoder().encodeToString(Resources.toByteArray(Resources.getResource("keypair/public_key.der")));
  }

  @Test
  public void verifyEncryptionAndDecryption() {
    for (int i = 0; i < 100; ++i) {
      final String generatedString = RandomStringUtils.random(32, true, true);
      final String encrypted = rsaCryptoUtilsEncryptor.encrypt(
          generatedString,
          publicKeyBase64);
      final String decrypted = rsaCryptoUtilsDecryptor.decrypt(
          encrypted,
          privateKeyBase64);

      assertNotEquals(encrypted, generatedString);
      assertNotEquals(encrypted, decrypted);
      assertEquals(generatedString, decrypted);
    }
  }
}
