package com.octopus.githuboauth;

/**
 * Constant values used by the project.
 */
public class Constants {

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
  /**
   * The cookie holding the access token.
   */
  public static final String SESSION_COOKIE = "GitHubSession";
}
