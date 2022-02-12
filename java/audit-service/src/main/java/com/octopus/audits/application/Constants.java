package com.octopus.audits.application;

/**
 * Constants used by the service.
 */
public final class Constants {

  public static final String JSONAPI_CONTENT_TYPE = "application/vnd.api+json";
  public static final String DATA_PARTITION_HEADER = "Data-Partition";
  public static final String ACCEPT = "Accept";
  public static final String AUTHORIZATION_HEADER = "Authorization";
  /**
   * A header used for machine to machine communication. This allows the user credentials to be
   * propagated with the Authorization header, which allows user specific actions to be allowed or
   * denied, while services communicate amongst themselves with the ServiceAuthorization header.
   *
   * <p>The more "correct" approach here is to use the OAuth On-Behalf-of token exchange, which would
   * allow services to call each other on behalf of the user. Unfortunately, Cognito does not
   * support OBO, so we need to work around the limitation by passing both user and machine tokens.
   */
  public static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
  public static final String FILTER_QUERY_PARAM = "filter";
  public static final String GET_METHOD = "get";
  public static final String POST_METHOD = "post";
}
