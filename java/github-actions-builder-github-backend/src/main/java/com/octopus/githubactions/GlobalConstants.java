package com.octopus.githubactions;

/**
 * Constants used by the service.
 */
public final class GlobalConstants {
  public static final String MICROSERVICE_NAME = "GithubActionWorkflowBuilder";
  public static final String JSONAPI_CONTENT_TYPE = "application/vnd.api+json";
  public static final String JSON_CONTENT_TYPE = "application/json";
  public static final String FROM_ENCODED_CONTENT_TYPE = "application/x-www-form-urlencoded";
  public static final String ACCEPT_HEADER = "Accept";
  public static final String ROUTING_HEADER = "Routing";
  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
  public static final String INVOCATION_TYPE = "X-Amz-Invocation-Type";
  public static final String ASYNC_INVOCATION_TYPE = "Event";
  public static final String CREATED_TEMPLATE_ACTION = "CreateTemplateUsing";
  public static final String CLIENT_CREDENTIALS = "client_credentials";
  public static final String AUDIT_SCOPE = "audit.content-team/admin";
}
