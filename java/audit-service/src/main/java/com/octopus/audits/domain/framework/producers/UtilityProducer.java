package com.octopus.audits.domain.framework.producers;

import com.octopus.audits.domain.jsonapi.AcceptHeaderVerifier;
import com.octopus.audits.domain.jsonapi.impl.VersionOneAcceptHeaderVerifier;
import com.octopus.lambda.CaseInsensitiveHttpHeaderExtractor;
import com.octopus.lambda.CaseInsensitiveLambdaHttpValueExtractor;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

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
   * Produces the "Accept" header verifier.
   *
   * @return An implementation of AcceptHeaderVerifier.
   */
  @ApplicationScoped
  @Produces
  public AcceptHeaderVerifier getAcceptHeaderVerifier() {
    return new VersionOneAcceptHeaderVerifier();
  }
}
