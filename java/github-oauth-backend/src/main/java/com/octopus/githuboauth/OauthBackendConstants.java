package com.octopus.githuboauth;

/**
 * Constant values used by the project.
 */
public final class OauthBackendConstants {

  /**
   * The query param holding the OAuth code value.
   */
  public static final String CODE_QUERY_PARAM = "code";
  /**
   * The query param holding the OAuth state value.
   */
  public static final String STATE_QUERY_PARAM = "state";
  /**
   * The cookie holding the state used to generate the OAuth code.
   */
  public static final String STATE_COOKIE = "GitHubState";
}
