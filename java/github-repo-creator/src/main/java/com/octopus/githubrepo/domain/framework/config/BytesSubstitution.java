package com.octopus.githubrepo.domain.framework.config;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import java.security.SecureRandom;
import software.pando.crypto.nacl.Bytes;

/**
 * Random classes must be initialized at runtime to generate a random seed. The encryption library
 * used a static SecureRandom in the Bytes class which must be replaced with a runtime initialized
 * version.
 */
@TargetClass(Bytes.class)
public final class BytesSubstitution {
  @Alias
  private static SecureRandom getSecureRandomInstance() {
    return null;
  }

  @Substitute
  public static byte[] secureRandom(int numBytes) {
    return SecureRandomLazyHolder.SECURE_RANDOM_SOURCE.generateSeed(numBytes);
  }

  /**
   * This idiom allows a static field to be initialized once at runtime when it is first accessed.
   */
  private static class SecureRandomLazyHolder {
    private static final SecureRandom SECURE_RANDOM_SOURCE;

    static {
      SECURE_RANDOM_SOURCE = getSecureRandomInstance();
    }
  }
}
