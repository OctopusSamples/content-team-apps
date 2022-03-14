package com.octopus.githubrepo.domain.framework.config;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.InjectAccessors;
import com.oracle.svm.core.annotate.TargetClass;
import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.internal.DefaultGitHubConnector;

/**
 * The GitHubConnector interface exposes a public static final property called DEFAULT, which in
 * turn references DefaultGitHubConnector.create(), which then goes on to create a HTTP client.
 * This results in the natve compilation error:
 * No instances of javax.net.ssl.SSLContext are allowed in the image heap as this class should be initialized at image runtime.
 *
 * The solution is to substitute an accessor for this field, which creates the GitHubConnector
 * at runtime.
 *
 * See https://stackoverflow.com/questions/63328298/how-do-you-debug-a-no-instances-of-are-allowed-in-the-image-heap-when-buil.
 */
@TargetClass(GitHubConnector.class)
public class GitHubConnectorSubstitution {
  @Alias
  @InjectAccessors(GitHubConnectorAccessor.class)
  private static GitHubConnector DEFAULT;

  private static class GitHubConnectorAccessor {
    static GitHubConnector get() {
      // using https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
      return GitHubConnectorLazyHolder.DEFAULT;
    }

    static void set(GitHubConnector ignored) {
      // a no-op setter to avoid exceptions when NetUtil is initialized at run-time
    }
  }

  private static class GitHubConnectorLazyHolder {
    private static final GitHubConnector DEFAULT;

    static {
        DEFAULT = DefaultGitHubConnector.create();
    }
  }
}
