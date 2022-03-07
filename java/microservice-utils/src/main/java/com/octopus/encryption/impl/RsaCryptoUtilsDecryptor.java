package com.octopus.encryption.impl;

import com.octopus.encryption.AsymmetricDecryptor;
import com.octopus.exceptions.EncryptionException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

/**
 * A service that can decrypt values with asymmetric key pairs.
 * https://mkyong.com/java/java-asymmetric-cryptography-example/
 * https://gist.github.com/mcasperson/92e8b9c38793cc830bbbbcf094ce63f6
 */
public class RsaCryptoUtilsDecryptor implements AsymmetricDecryptor {

  private final Cipher cipher;

  public RsaCryptoUtilsDecryptor()
      throws NoSuchPaddingException, NoSuchAlgorithmException {
    this.cipher = Cipher.getInstance("RSA");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String decrypt(final String value, final String privateKeyBase64) {
    try {
      this.cipher.init(Cipher.DECRYPT_MODE, getPrivate(privateKeyBase64));
      return new String(this.cipher.doFinal(Base64.getDecoder().decode(value)));
    } catch (Exception e) {
      throw new EncryptionException(e);
    }
  }

  // https://docs.oracle.com/javase/8/docs/api/java/security/spec/X509EncodedKeySpec.html
  private PrivateKey getPrivate(final String key) throws Exception {
    final byte[] keyBytes = Base64.getDecoder().decode(key);
    final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    final KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePrivate(spec);
  }
}
