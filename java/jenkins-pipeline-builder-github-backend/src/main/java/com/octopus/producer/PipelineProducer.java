package com.octopus.producer;

import com.octopus.builders.PipelineBuilder;
import com.octopus.builders.dotnet.DotnetCoreBuilder;
import com.octopus.builders.go.GoBuilder;
import com.octopus.builders.java.JavaGradleBuilder;
import com.octopus.builders.java.JavaMavenBuilder;
import com.octopus.builders.nodejs.NodejsBuilder;
import com.octopus.builders.php.PhpComposerBuilder;
import com.octopus.builders.python.PythonBuilder;
import com.octopus.builders.ruby.RubyGemBuilder;
import com.octopus.http.HttpClient;
import com.octopus.http.StringHttpClient;
import com.octopus.repoclients.RepoClient;
import com.octopus.repoclients.github.GithubRepoClient;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Generates CDI beans to be used in the pipeline generation. Be aware the not all scopes are used
 * by all end points. For example, @RequestScoped doesn't work with Lambdas.
 */
@ApplicationScoped
public class PipelineProducer {

  @ConfigProperty(name = "application.github-client-id", defaultValue = "")
  Optional<String> clientId;

  @ConfigProperty(name = "application.github-client-secret", defaultValue = "")
  Optional<String> clientSecret;

  /**
   * Produces the HTTP client.
   *
   * @return An implementation of HttpClient.
   */
  @ApplicationScoped
  @Produces
  public HttpClient getHttpClient() {
    return new StringHttpClient();
  }

  /**
   * Produces the repository accessor.
   *
   * @return An implementation of RepoAccessor.
   */
  @Produces
  public RepoClient getRepoAccessor(final HttpClient httpClient) {
    return GithubRepoClient.builder()
        .httpClient(httpClient)
        .username(clientId.orElse(""))
        .password(clientSecret.orElse(""))
        .build();
  }

  /**
   * Produces the Maven pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getMavenBuilder() {
    return new JavaMavenBuilder();
  }

  /**
   * Produces the Gradle pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getGradleBuilder() {
    return new JavaGradleBuilder();
  }

  /**
   * Produces the DotNet Core pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getDotNetCoreBuilder() {
    return new DotnetCoreBuilder();
  }

  /**
   * Produces the PHP pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getPhpComposerBuilder() {
    return new PhpComposerBuilder();
  }

  /**
   * Produces the Python pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getPythonBuilder() {
    return new PythonBuilder();
  }

  /**
   * Produces the Ruby pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getRubyBuilder() {
    return new RubyGemBuilder();
  }

  /**
   * Produces the Go pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getGoBuilder() {
    return new GoBuilder();
  }

  /**
   * Produces the Node.js pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getNodejsNpmBuilder() {
    return new NodejsBuilder();
  }
}
