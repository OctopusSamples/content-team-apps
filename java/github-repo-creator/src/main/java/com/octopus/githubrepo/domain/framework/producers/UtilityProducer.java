package com.octopus.githubrepo.domain.framework.producers;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.encryption.AsymmetricDecryptor;
import com.octopus.encryption.CryptoUtils;
import com.octopus.encryption.impl.AesCryptoUtils;
import com.octopus.encryption.impl.RsaCryptoUtilsDecryptor;
import com.octopus.features.AdminJwtGroupFeature;
import com.octopus.features.CognitoJwkBase64Feature;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.githubrepo.domain.entities.GenerateTemplate;
import com.octopus.githubrepo.domain.utils.impl.JsonApiServiceUtilsGenerateTemplate;
import com.octopus.jsonapi.AcceptHeaderVerifier;
import com.octopus.jsonapi.PagedResultsLinksBuilder;
import com.octopus.jsonapi.impl.PagedResultsLinksBuilderImpl;
import com.octopus.jsonapi.impl.VersionOneAcceptHeaderVerifier;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import com.octopus.jwt.JwtValidator;
import com.octopus.jwt.impl.JoseJwtInspector;
import com.octopus.jwt.impl.JwtUtilsImpl;
import com.octopus.jwt.impl.JwtValidatorImpl;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import com.octopus.lambda.ProxyResponseBuilder;
import com.octopus.lambda.impl.CaseInsensitiveCookieExtractor;
import com.octopus.lambda.impl.CaseInsensitiveHttpHeaderExtractor;
import com.octopus.lambda.impl.CaseInsensitiveLambdaHttpValueExtractor;
import com.octopus.lambda.impl.ProxyResponseBuilderImpl;
import com.octopus.githubrepo.domain.entities.CreateGithubRepo;
import com.octopus.githubrepo.domain.utils.JsonApiResourceUtils;
import com.octopus.githubrepo.domain.utils.impl.JsonApiServiceUtilsCreateGithubRepo;
import com.octopus.utilties.PartitionIdentifier;
import com.octopus.utilties.RegExUtils;
import com.octopus.utilties.impl.PartitionIdentifierImpl;
import com.octopus.utilties.impl.RegExUtilsImpl;
import java.security.NoSuchAlgorithmException;
import javax.crypto.NoSuchPaddingException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

/**
 * Produces a number of objects for injection.
 */
@ApplicationScoped
public class UtilityProducer {

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
   * Produces the "Accept" header verifier.
   *
   * @return An implementation of AcceptHeaderVerifier.
   */
  @ApplicationScoped
  @Produces
  public AcceptHeaderVerifier getAcceptHeaderVerifier() {
    return new VersionOneAcceptHeaderVerifier();
  }

  /**
   * Produces proxy response builder.
   *
   * @return An implementation of ProxyResponseBuilder.
   */
  @ApplicationScoped
  @Produces
  public ProxyResponseBuilder getProxyResponseBuilder() {
    return new ProxyResponseBuilderImpl();
  }

  /**
   * Produces regex utils instance.
   *
   * @return An implementation of RegExUtils.
   */
  @ApplicationScoped
  @Produces
  public RegExUtils getRegExUtils() {
    return new RegExUtilsImpl();
  }

  /**
   * Produces paged results link builder.
   *
   * @return An implementation of PagedResultsLinksBuilder.
   */
  @ApplicationScoped
  @Produces
  public PagedResultsLinksBuilder getPagedResultsLinksBuilder() {
    return new PagedResultsLinksBuilderImpl();
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
   * Produces the JWT utils service.
   *
   * @return An implementation of JwtUtils.
   */
  @ApplicationScoped
  @Produces
  public JwtUtils jwtUtils() {
    return new JwtUtilsImpl();
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

  /**
   * Produces a JsonApiResourceUtils typed for CreateServiceAccount.
   *
   * @param resourceConverter The JSON api resource converter.
   * @return An instance of JsonApiResourceUtils.
   */
  @ApplicationScoped
  @Produces
  @Named("JsonApiServiceUtilsCreateGithubRepo")
  public JsonApiResourceUtils<CreateGithubRepo> jsonApiServiceUtilsCreateGithubRepo(
      final ResourceConverter resourceConverter) {
    return new JsonApiServiceUtilsCreateGithubRepo(resourceConverter);
  }

  /**
   * Produces a JsonApiResourceUtils typed for CreateServiceAccount.
   *
   * @param resourceConverter The JSON api resource converter.
   * @return An instance of JsonApiResourceUtils.
   */
  @ApplicationScoped
  @Produces
  @Named("JsonApiServiceUtilsGenerateTemplate")
  public JsonApiResourceUtils<GenerateTemplate> jsonApiServiceUtilsGenerateTemplate(
      final ResourceConverter resourceConverter) {
    return new JsonApiServiceUtilsGenerateTemplate(resourceConverter);
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
   * Produces the AsymmetricDecryptor utils instance.
   *
   * @return An implementation of AsymmetricDecryptor.
   */
  @ApplicationScoped
  @Produces
  public AsymmetricDecryptor getAsymmetricDecryptor ()
      throws NoSuchPaddingException, NoSuchAlgorithmException {
    return new RsaCryptoUtilsDecryptor();
  }
}
