package com.octopus.encryption.impl;

import com.octopus.encryption.AsymmetricEncryptor;
import com.octopus.exceptions.EncryptionException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

/**
 * A service that can encrypt values with asymmetric key pairs.
 * https://mkyong.com/java/java-asymmetric-cryptography-example/
 * https://gist.github.com/mcasperson/92e8b9c38793cc830bbbbcf094ce63f6
 */
public class RsaCryptoUtilsEncryptor implements AsymmetricEncryptor {

  private final Cipher cipher;

  public RsaCryptoUtilsEncryptor()
      throws NoSuchPaddingException, NoSuchAlgorithmException {
    this.cipher = Cipher.getInstance("RSA");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String encrypt(final String value, final String publicKeyBase64) {
    try {
      this.cipher.init(Cipher.ENCRYPT_MODE, getPublic(publicKeyBase64));
      return Base64.getEncoder().encodeToString(this.cipher.doFinal(value.getBytes()));
    } catch (Exception e) {
      throw new EncryptionException(e);
    }
  }

  // https://docs.oracle.com/javase/8/docs/api/java/security/spec/X509EncodedKeySpec.html
  private PublicKey getPublic(final String key) throws Exception {
    byte[] keyBytes = Base64.getDecoder().decode(key);
    final X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
    final KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePublic(spec);
  }
}
