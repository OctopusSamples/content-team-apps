package com.octopus.githubactions.github.domain.framework.producer;

import com.octopus.builders.PipelineBuilder;
import com.octopus.encryption.AsymmetricEncryptor;
import com.octopus.encryption.CryptoUtils;
import com.octopus.encryption.impl.AesCryptoUtils;
import com.octopus.encryption.impl.RsaCryptoUtilsEncryptor;
import com.octopus.githubactions.shared.builders.DotNetCoreBuilder;
import com.octopus.githubactions.shared.builders.GenericBuilder;
import com.octopus.githubactions.shared.builders.GoBuilder;
import com.octopus.githubactions.shared.builders.JavaGradleBuilder;
import com.octopus.githubactions.shared.builders.JavaMavenBuilder;
import com.octopus.githubactions.shared.builders.NodeJsBuilder;
import com.octopus.githubactions.shared.builders.PhpComposerBuilder;
import com.octopus.githubactions.shared.builders.PythonBuilder;
import com.octopus.githubactions.shared.builders.RubyBuilder;
import com.octopus.githubactions.github.domain.features.ServiceBusCognitoConfig;
import com.octopus.githubactions.github.infrastructure.client.CognitoClient;
import com.octopus.http.ReadOnlyHttpClient;
import com.octopus.http.impl.ReadOnlyHttpClientImpl;
import com.octopus.json.JsonSerializer;
import com.octopus.json.impl.JacksonJsonSerializerImpl;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import com.octopus.lambda.impl.CaseInsensitiveCookieExtractor;
import com.octopus.lambda.impl.CaseInsensitiveHttpHeaderExtractor;
import com.octopus.lambda.impl.CaseInsensitiveLambdaHttpValueExtractor;
import com.octopus.oauth.OauthClientCredsAccessor;
import com.octopus.oauth.impl.OauthClientCredsAccessorImpl;
import com.octopus.repoclients.RepoClientFactory;
import com.octopus.repoclients.impl.GitHubRepoClientFactory;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import javax.crypto.NoSuchPaddingException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

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

  @RestClient
  CognitoClient cognitoClient;

  @Inject
  ServiceBusCognitoConfig serviceBusCognitoConfig;

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
  @ApplicationScoped
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
   * Produces the JSON serializer.
   *
   * @return An implementation of JsonSerializer.
   */
  @ApplicationScoped
  @Produces
  public JsonSerializer getJsonSerializer() {
    return new JacksonJsonSerializerImpl();
  }

  /**
   * Produces the Oauth client creds token generator.
   *
   * @return An implementation of OauthClientCredsAccessor.
   */
  @ApplicationScoped
  @Produces
  public OauthClientCredsAccessor getOauthClientCredsAccessor() {
    return new OauthClientCredsAccessorImpl(serviceBusCognitoConfig, cognitoClient);
  }
}