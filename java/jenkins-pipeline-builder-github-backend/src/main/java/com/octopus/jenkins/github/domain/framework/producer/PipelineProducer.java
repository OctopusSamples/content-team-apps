package com.octopus.jenkins.github.domain.framework.producer;

import com.octopus.builders.PipelineBuilder;
import com.octopus.features.AdminJwtGroupFeature;
import com.octopus.features.CognitoJwkBase64Feature;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.jenkins.shared.builders.dotnet.DotnetCoreBuilder;
import com.octopus.jenkins.shared.builders.generic.GenericBuilder;
import com.octopus.jenkins.shared.builders.go.GoBuilder;
import com.octopus.jenkins.shared.builders.java.JavaGradleBuilder;
import com.octopus.jenkins.shared.builders.java.JavaMavenBuilder;
import com.octopus.jenkins.shared.builders.nodejs.NodejsBuilder;
import com.octopus.jenkins.shared.builders.php.PhpComposerBuilder;
import com.octopus.jenkins.shared.builders.python.PythonBuilder;
import com.octopus.jenkins.shared.builders.ruby.RubyGemBuilder;
import com.octopus.encryption.AsymmetricEncryptor;
import com.octopus.encryption.CryptoUtils;
import com.octopus.encryption.impl.AesCryptoUtils;
import com.octopus.encryption.impl.RsaCryptoUtilsEncryptor;
import com.octopus.http.ReadOnlyHttpClient;
import com.octopus.http.impl.ReadOnlyHttpClientImpl;
import com.octopus.jenkins.github.domain.features.ServiceBusCognitoConfig;
import com.octopus.jenkins.github.infrastructure.client.CognitoClient;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtValidator;
import com.octopus.jwt.impl.JoseJwtInspector;
import com.octopus.jwt.impl.JwtValidatorImpl;
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
import com.octopus.utilties.PartitionIdentifier;
import com.octopus.utilties.impl.PartitionIdentifierImpl;
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
    return new NodejsBuilder();
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
    return new RubyGemBuilder();
  }

  /**
   * Produces the DotNET Core pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getDotNetCore() {
    return new DotnetCoreBuilder();
  }

  /**
   * Produces the generic pipeline builder.
   *
   * @return An implementation of PipelineBuilder.
   */
  @ApplicationScoped
  @Produces
  public PipelineBuilder getGenericBuilder() {
    return new GenericBuilder();
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

  /**
   * Produces the JWT validator.
   *
   * @return An implementation of JwtValidator.
   */
  @ApplicationScoped
  @Produces
  public JwtValidator getJwtValidator() {
    return new JwtValidatorImpl();
  }

  /**
   * Produces the JWT verification service.
   *
   * @return An implementation of JwtVerifier.
   */
  @ApplicationScoped
  @Produces
  public JwtInspector getJwtInspector(
      CognitoJwkBase64Feature cognitoJwkBase64Feature,
      DisableSecurityFeature disableSecurityFeature,
      JwtValidator jwtValidator,
      MicroserviceNameFeature microserviceNameFeature) {
    return new JoseJwtInspector(
        cognitoJwkBase64Feature,
        disableSecurityFeature,
        jwtValidator,
        microserviceNameFeature);
  }

  /**
   * Produces the data partition identifier service.
   *
   * @return An implementation of PartitionIdentifier.
   */
  @ApplicationScoped
  @Produces
  public PartitionIdentifier getPartitionIdentifier(
      JwtInspector jwtInspector,
      AdminJwtGroupFeature adminJwtGroupFeature,
      DisableSecurityFeature disableSecurityFeature) {
    return new PartitionIdentifierImpl(jwtInspector, adminJwtGroupFeature, disableSecurityFeature);
  }
}
