package com.octopus.encryption.impl;

import com.google.common.io.Resources;
import com.octopus.encryption.CryptoUtils;
import com.octopus.exceptions.EncryptionException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.lang3.NotImplementedException;

/**
 * A service that can encrypt values with asymmetric key pairs. https://mkyong.com/java/java-asymmetric-cryptography-example/
 */
public class RsaCryptoUtilsEncryptor implements CryptoUtils {

  private final Cipher cipher;

  public RsaCryptoUtilsEncryptor()
      throws NoSuchPaddingException, NoSuchAlgorithmException {
      this.cipher = Cipher.getInstance("RSA");
  }

  /** {@inheritDoc} */
  @Override
  public String encrypt(final String value, final String publicKeyPath, final String salt) {
    try {
      this.cipher.init(Cipher.ENCRYPT_MODE, getPublic(publicKeyPath));
      return Base64.getEncoder().encodeToString(this.cipher.doFinal(value.getBytes()));
    } catch (Exception e) {
      throw new EncryptionException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String decrypt(final String value, final String privateKey, final String salt) {
    throw new NotImplementedException();
  }

  // https://docs.oracle.com/javase/8/docs/api/java/security/spec/X509EncodedKeySpec.html
  private PublicKey getPublic(final String key) throws Exception {
    byte[] keyBytes = Resources.toByteArray(Resources.getResource(key));
    final X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
    final KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePublic(spec);
  }
}
