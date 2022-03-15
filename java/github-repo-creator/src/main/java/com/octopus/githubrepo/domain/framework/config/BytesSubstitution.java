package com.octopus.githubrepo.domain.framework.config;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import java.security.SecureRandom;
import software.pando.crypto.nacl.Bytes;

/**
 * Random classes must be initialized at runtime to generate a unique random seed. The encryption
 * library used a static SecureRandom in the Bytes class which would have been initialized at
 * compile time. It must be replaced with a property initialized at runtime.
 */
@TargetClass(Bytes.class)
public final class BytesSubstitution {

  /**
   * This is how we access a private method in the Bytes class.
   */
  @Alias
  private static SecureRandom getSecureRandomInstance() {
    return null;
  }

  /**
   * Substitute the secureRandom method with one that initializes SECURE_RANDOM_SOURCE at runtime
   * when it is first used.
   */
  @Substitute
  public static byte[] secureRandom(int numBytes) {
    return SecureRandomLazyHolder.SECURE_RANDOM_SOURCE.generateSeed(numBytes);
  }

  /**
   * This idiom allows a static field to be initialized once at runtime when it is first accessed.
   */
  private static class SecureRandomLazyHolder {

    private static final SecureRandom SECURE_RANDOM_SOURCE = getSecureRandomInstance();
  }
}
