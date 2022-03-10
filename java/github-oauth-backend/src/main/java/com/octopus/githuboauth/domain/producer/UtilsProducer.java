package com.octopus.githuboauth.domain.producer;

import com.octopus.encryption.CryptoUtils;
import com.octopus.encryption.impl.AesCryptoUtils;
import com.octopus.http.CookieDateUtils;
import com.octopus.http.impl.CookieDateUtilsImpl;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import com.octopus.lambda.impl.CaseInsensitiveCookieExtractor;
import com.octopus.lambda.impl.CaseInsensitiveLambdaHttpValueExtractor;
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
   * Produces the cookie date utils instance.
   *
   * @return An implementation of CookieDateUtils.
   */
  @ApplicationScoped
  @Produces
  public CookieDateUtils getCookieDateUtils() {
    return new CookieDateUtilsImpl();
  }
}
