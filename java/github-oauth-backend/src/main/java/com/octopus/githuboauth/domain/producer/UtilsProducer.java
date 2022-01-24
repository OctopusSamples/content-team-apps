package com.octopus.githuboauth.domain.producer;

import com.octopus.lambda.CaseInsensitiveQueryParamExtractor;
import com.octopus.lambda.QueryParamExtractor;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class UtilsProducer {

  /**
   * Produces the Lambda query param extractor.
   *
   * @return An implementation of QueryParamExtractor.
   */
  @ApplicationScoped
  @Produces
  public QueryParamExtractor getQueryParamExtractor() {
    return new CaseInsensitiveQueryParamExtractor();
  }
}
