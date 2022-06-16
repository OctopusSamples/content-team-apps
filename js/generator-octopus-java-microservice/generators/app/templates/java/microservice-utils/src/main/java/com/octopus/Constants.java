package com.octopus;

/**
 * Defines constant values used across multiple microservices.
 */
public final class Constants {

  public static final String DEFAULT_PARTITION = "main";
  public static final int DEFAULT_PAGE_LIMIT = 30;
  public static final int DEFAULT_PAGE_OFFSET = 0;
  public static final String DATA_PARTITION_HEADER = "Data-Partition";
  /**
   * A header used for machine to machine communication. This allows the user credentials to be
   * propagated with the Authorization header, which allows user specific actions to be allowed or
   * denied, while services communicate amongst themselves with the Service-Authorization header.
   *
   * <p>The more "correct" approach here is to use the OAuth On-Behalf-of token exchange, which would
   * allow services to call each other on behalf of the user. Unfortunately, Cognito does not
   * support OBO, so we need to work around the limitation by passing both user and machine tokens.
   */
  public static final String SERVICE_AUTHORIZATION_HEADER = "Service-Authorization";

  public static final String ROUTING_HEADER = "Routing";

  public static final String AMAZON_TRACE_ID_HEADER = "X-Amzn-Trace-Id";

  /**
   * Constants relating to JSONAPI.
   */
  public final class JsonApi {
    public static final String JSONAPI_CONTENT_TYPE = "application/vnd.api+json";
    public static final String FILTER_QUERY_PARAM = "filter";
    public static final String PAGE_OFFSET_QUERY_PARAM = "page[offset]";
    public static final String PAGE_LIMIT_QUERY_PARAM = "page[limit]";
  }

  /**
   * HTTP constants.
   */
  public final class Http {
    public static final String GET_METHOD = "get";
    public static final String POST_METHOD = "post";
    public static final String OPTIONS_METHOD = "options";
  }
}
