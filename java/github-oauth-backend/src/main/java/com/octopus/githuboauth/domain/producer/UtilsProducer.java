package com.octopus.githuboauth.domain.producer;

import com.octopus.encryption.AesCryptoUtils;
import com.octopus.encryption.CryptoUtils;
import com.octopus.lambda.CaseInsensitiveLambdaHttpValueExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * Creates common utility objects for composition into other services.
 */
@ApplicationScoped
public class UtilsProducer {

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
   * Produces the crypto utils instance.
   *
   * @return An implementation of CryptoUtils.
   */
  @ApplicationScoped
  @Produces
  public CryptoUtils getCryptoUtils() {
    return new AesCryptoUtils();
  }
}
