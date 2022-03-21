package com.octopus.loginmessage.domain.framework.producers;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.octopus.loginmessage.domain.servicebus.AzureServiceBus;
import com.octopus.features.AdminJwtGroupFeature;
import com.octopus.features.CognitoJwkBase64Feature;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.json.JsonSerializer;
import com.octopus.json.impl.JacksonJsonSerializerImpl;
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
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import com.octopus.lambda.ProxyResponseBuilder;
import com.octopus.lambda.impl.CaseInsensitiveHttpHeaderExtractor;
import com.octopus.lambda.impl.CaseInsensitiveLambdaHttpValueExtractor;
import com.octopus.lambda.impl.ProxyResponseBuilderImpl;
import com.octopus.utilties.PartitionIdentifier;
import com.octopus.utilties.RegExUtils;
import com.octopus.utilties.impl.PartitionIdentifierImpl;
import com.octopus.utilties.impl.RegExUtilsImpl;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;

/**
 * Produces a number of objects for injection.
 */
@ApplicationScoped
public class UtilityProducer {

  @Inject
  AzureServiceBus azureServiceBus;

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
   * Produces an azure service bus sender.
   *
   * <p>Can't be scoped: https://github.com/quarkusio/quarkus/issues/13146
   *
   * @return The azure service bus sender, or empty if the configuration is not available.
   */
  @Produces
  public ServiceBusSenderClient generateAzureServiceBusSender() {
    if (azureServiceBus.getCredentials().isEmpty()
        || azureServiceBus.getNamespace().isEmpty()
        || StringUtils.isBlank(azureServiceBus.getNamespace().get())
        || azureServiceBus.getTopic().isEmpty()
        || StringUtils.isBlank(azureServiceBus.getTopic().get())) {
      return null;
    }

    return new ServiceBusClientBuilder()
        .credential(azureServiceBus.getNamespace().get(), azureServiceBus.getCredentials().get())
        .sender()
        .topicName(azureServiceBus.getTopic().get())
        .buildClient();
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
}
