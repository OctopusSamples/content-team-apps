package com.octopus.githubrepo.domain.framework.config;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.InjectAccessors;
import com.oracle.svm.core.annotate.TargetClass;
import org.kohsuke.github.connector.GitHubConnector;

/**
 * The GitHubConnector interface exposes a public static final property called DEFAULT, which in
 * turn references DefaultGitHubConnector.create(), which then goes on to create a HTTP client.
 * This results in the native compilation error:
 * No instances of javax.net.ssl.SSLContext are allowed in the image heap as this class should be initialized at image runtime.
 *
 * <p>The solution is to substitute an accessor for this field, which returns null. We then must
 * set the connector ourselves at runtime when building a GitHubBuilder.
 *
 * <p>See https://stackoverflow.com/questions/63328298/how-do-you-debug-a-no-instances-of-are-allowed-in-the-image-heap-when-buil.
 */
@TargetClass(GitHubConnector.class)
public final class GitHubConnectorSubstitution {
  @Alias
  @InjectAccessors(GitHubConnectorAccessor.class)
  private static GitHubConnector DEFAULT;

  private static class GitHubConnectorAccessor {
    static GitHubConnector get() {
      return null;
    }

    static void set(GitHubConnector ignored) {
      // a no-op setter
    }
  }
}
