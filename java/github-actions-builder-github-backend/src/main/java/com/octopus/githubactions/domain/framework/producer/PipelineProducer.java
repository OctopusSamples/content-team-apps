package com.octopus.githubactions.domain.framework.producer;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.octopus.builders.PipelineBuilder;
import com.octopus.encryption.AsymmetricEncryptor;
import com.octopus.encryption.CryptoUtils;
import com.octopus.encryption.impl.AesCryptoUtils;
import com.octopus.encryption.impl.RsaCryptoUtilsEncryptor;
import com.octopus.githubactions.GlobalConstants;
import com.octopus.githubactions.builders.DotNetCoreBuilder;
import com.octopus.githubactions.builders.GenericBuilder;
import com.octopus.githubactions.builders.GoBuilder;
import com.octopus.githubactions.builders.JavaGradleBuilder;
import com.octopus.githubactions.builders.JavaMavenBuilder;
import com.octopus.githubactions.builders.NodeJsBuilder;
import com.octopus.githubactions.builders.PhpComposerBuilder;
import com.octopus.githubactions.builders.PythonBuilder;
import com.octopus.githubactions.builders.RubyBuilder;
import com.octopus.githubactions.domain.features.AzureServiceBus;
import com.octopus.http.ReadOnlyHttpClient;
import com.octopus.http.impl.ReadOnlyHttpClientImpl;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import com.octopus.lambda.impl.CaseInsensitiveCookieExtractor;
import com.octopus.lambda.impl.CaseInsensitiveHttpHeaderExtractor;
import com.octopus.lambda.impl.CaseInsensitiveLambdaHttpValueExtractor;
import com.octopus.repoclients.RepoClientFactory;
import com.octopus.repoclients.impl.GitHubRepoClientFactory;
import io.quarkus.logging.Log;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import javax.crypto.NoSuchPaddingException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
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

  @Inject
  AzureServiceBus azureServiceBus;

  /**
   * Produces the HTTP client.
   *
   * @return An implementation of HttpClient.
   */
  @ApplicationScoped
  @Produces
  public ReadOnlyHttpClient getHttpClient() {
    return new ReadOnlyHttpClientImpl();
  }

  /**
   * Produces the repository accessor factory.
   *
   * @return An implementation of RepoAccessor.
   */
  @Produces
  public RepoClientFactory getRepoClientFactory(final ReadOnlyHttpClient readOnlyHttpClient) {
    return GitHubRepoClientFactory.builder()
        .readOnlyHttpClient(readOnlyHttpClient)
        .username(clientId.orElse(""))
        .password(clientSecret.orElse(""))
        .build();
  }

  /**
   * Produces the crypto utils instance.
   *
   * @return An implementation of CryptoUtils.
   */
  @ApplicationScoped
  @Produces
  public CryptoUtils getCryptoUtils() {
    return new AesCryptoUtils();
  }

  /**
   * Produces the AsymmetricEncryptor.
   *
   * @return An implementation of AsymmetricEncryptor.
   */
  @ApplicationScoped
  @Produces
  public AsymmetricEncryptor getAsymmetricEncryptor()
      throws NoSuchPaddingException, NoSuchAlgorithmException {
    return new RsaCryptoUtilsEncryptor();
  }

  /**
   * Produces the Lambda query param extractor.
   *
   * @return An implementation of QueryParamExtractor.
   */
  @ApplicationScoped
  @Produces
  public LambdaHttpHeaderExtractor getHeaderExtractor() {
    return new CaseInsensitiveHttpHeaderExtractor();
  }

  /**
   * Produces the Lambda query param extractor.
   *
   * @return An implementation of QueryParamExtractor.
   */
  @ApplicationScoped
  @Produces
  public LambdaHttpValueExtractor getQueryParamExtractor() {
    return new CaseInsensitiveLambdaHttpValueExtractor();
  }

  /**
   * Produces the Lambda cookie extractor.
   *
   * @return An implementation of QueryParamExtractor.
   */
  @ApplicationScoped
  @Produces
  public LambdaHttpCookieExtractor getCookieExtractor() {
    return new CaseInsensitiveCookieExtractor();
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
   * Produces the Node.js pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getNodeJsBuilder() {
    return new NodeJsBuilder();
  }

  /**
   * Produces the PHP pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getPhpBuilder() {
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
   * Produces the Ruby pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getRubyBuilder() {
    return new RubyBuilder();
  }

  /**
   * Produces the DotNET Core pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getDotNetCore() {
    return new DotNetCoreBuilder();
  }

  /**
   * Produces the generic pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getGeneric() {
    return new GenericBuilder();
  }

  /**
   * Produces an azure service bus sender.
   *
   * @return The azure service bus sender, or empty if the configuration is not available.
   */
  @ApplicationScoped
  public Optional<ServiceBusSenderClient> generateAzureServiceBusSender() {
    if (azureServiceBus.getCredentials().isEmpty() || azureServiceBus.getNamespace().isEmpty()
        || azureServiceBus.getTopic().isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new ServiceBusClientBuilder()
        .credential(azureServiceBus.getNamespace().get(), azureServiceBus.getCredentials().get())
        .sender()
        .topicName(azureServiceBus.getTopic().get())
        .buildClient());
  }
}
