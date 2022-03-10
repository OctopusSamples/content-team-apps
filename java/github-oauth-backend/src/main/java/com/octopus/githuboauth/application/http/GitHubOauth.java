package com.octopus.githuboauth.application.http;

import com.octopus.githuboauth.OauthBackendConstants;
import com.octopus.githuboauth.domain.handlers.GitHubOauthLoginHandler;
import com.octopus.githuboauth.domain.handlers.GitHubOauthRedirect;
import com.octopus.githuboauth.domain.handlers.SimpleResponse;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * WHen this app is run as a web server, this class defines the REST API endpoints.
 */
@Path("/oauth/github")
@RequestScoped
public class GitHubOauth {

  @Inject
  GitHubOauthLoginHandler gitHubOauthLoginHandler;

  @Inject
  GitHubOauthRedirect gitHubOauthRedirect;

  /**
   * Redirect the user to GitHub.
   *
   * @return a HTTP response object.
   */
  @GET
  @Path("login")
  public Response githubLogin() {
    final SimpleResponse simpleResponse = gitHubOauthLoginHandler.oauthLoginRedirect();
    final ResponseBuilder response = Response.status(simpleResponse.getCode(),
        simpleResponse.getBody());
    simpleResponse.getHeaders().forEach(response::header);
    return response.build();
  }

  /**
   * The redirection back to the app.
   *
   * @param state      The state that was returned by the GitHub login.
   * @param savedState The state that was persisted before the login.
   * @param code       The code that was returned by the GitHub login.
   * @return An HTTP response object with the created resource.
   */
  @GET
  @Path("code")
  public Response githubReturn(
      final String document,
      @QueryParam(OauthBackendConstants.STATE_QUERY_PARAM) final String state,
      @CookieParam(OauthBackendConstants.STATE_COOKIE) final String savedState,
      @QueryParam(OauthBackendConstants.CODE_QUERY_PARAM) final String code) {
    final SimpleResponse simpleResponse = gitHubOauthRedirect.oauthRedirect(state,
        List.of(savedState), code);
    final ResponseBuilder response = Response.status(simpleResponse.getCode(),
        simpleResponse.getBody());
    simpleResponse.getHeaders().forEach(response::header);
    simpleResponse.getMultiValueHeaders()
        .forEach((key, value) -> value.forEach(v -> response.header(key, v)));
    return response.build();
  }
}
