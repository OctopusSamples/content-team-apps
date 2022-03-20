package com.octopus.encryption.impl;

import com.octopus.encryption.CryptoUtils;
import com.octopus.exceptions.EncryptionException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 * Use AES to encrypt and decrypt strings.
 * https://mkyong.com/java/java-aes-encryption-and-decryption/
 */
public class AesCryptoUtils implements CryptoUtils {

  private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
  private static final int TAG_LENGTH_BIT = 128;
  private static final int IV_LENGTH_BYTE = 12;
  private static final String ALGORITHM = "AES";
  private static final int MIN_PASSWORD_LENGTH = 32;

  /**
   * {@inheritDoc}
   */
  public String encrypt(@NonNull final String value, @NonNull final String password,
      @NonNull final String salt) {
    try {
      if (StringUtils.isBlank(password) || StringUtils.isBlank(salt)) {
        throw new IllegalArgumentException("Password or salt can not be blank");
      }

      if (password.length() < MIN_PASSWORD_LENGTH || salt.length() < MIN_PASSWORD_LENGTH) {
        throw new IllegalArgumentException(
            "Password or salt must be at least " + MIN_PASSWORD_LENGTH + "characters");
      }

      final byte[] iv = getRandomNonce(IV_LENGTH_BYTE);
      final Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
      final SecretKey secretKey = getAesKeyFromPassword(
          password.toCharArray(),
          salt.getBytes(StandardCharsets.UTF_8));
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
      final byte[] cipherText = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
      final byte[] encryptedWithIv = ByteBuffer.allocate(iv.length + cipherText.length)
          .put(iv)
          .put(cipherText)
          .array();
      return Base64.getEncoder().encodeToString(encryptedWithIv);
    } catch (final NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                   | BadPaddingException | InvalidKeySpecException
                   | InvalidAlgorithmParameterException | InvalidKeyException e) {
      throw new EncryptionException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public String decrypt(@NonNull final String value, @NonNull final String password,
      @NonNull final String salt) {
    try {
      if (StringUtils.isBlank(password) || StringUtils.isBlank(salt)) {
        throw new IllegalArgumentException("Password or salt can not be blank");
      }

      if (StringUtils.isBlank(value)) {
        throw new IllegalArgumentException("Value can not be blank");
      }

      final ByteBuffer bb = ByteBuffer.wrap(Base64.getDecoder().decode(value));
      final byte[] iv = new byte[IV_LENGTH_BYTE];
      bb.get(iv);
      final byte[] cipherText = new byte[bb.remaining()];
      bb.get(cipherText);
      final SecretKey secretKey = getAesKeyFromPassword(
          password.toCharArray(),
          salt.getBytes(StandardCharsets.UTF_8));
      final Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
      return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    } catch (final NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException
                   | BadPaddingException | InvalidKeySpecException
                   | InvalidAlgorithmParameterException | InvalidKeyException e) {
      throw new EncryptionException(e);
    }
  }

  private SecretKey getAesKeyFromPassword(final char[] password, final byte[] salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    final KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
    return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ALGORITHM);
  }

  private byte[] getRandomNonce(final int numBytes) {
    final byte[] nonce = new byte[numBytes];
    new SecureRandom().nextBytes(nonce);
    return nonce;
  }
}
