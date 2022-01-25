package com.octopus.githuboauth.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.octopus.encryption.CryptoUtils;
import com.octopus.githuboauth.Constants;
import com.octopus.githuboauth.domain.oauth.OauthResponse;
import com.octopus.githuboauth.infrastructure.client.GitHubOauth;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import com.octopus.lambda.ProxyResponse;
import io.quarkus.logging.Log;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;


/**
 * This lambda handles the conversion of a code to an access token. https://docs.github.com/en/developers/apps/building-github-apps/identifying-and-authorizing-users-for-github-apps#2-users-are-redirected-back-to-your-site-by-github
 */
@Named("accessToken")
public class GitHubOauthRedirectLambda implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @ConfigProperty(name = "github.client.redirect")
  String clientRedirect;

  @ConfigProperty(name = "github.client.id")
  String clientId;

  @ConfigProperty(name = "github.client.secret")
  String clientSecret;

  @ConfigProperty(name = "github.encryption")
  String githubEncryption;

  @ConfigProperty(name = "github.salt")
  String githubSalt;

  @Inject
  LambdaHttpValueExtractor lambdaHttpValueExtractor;

  @Inject
  LambdaHttpCookieExtractor lambdaHttpCookieExtractor;

  @RestClient
  GitHubOauth gitHubOauth;

  @Inject
  CryptoUtils cryptoUtils;

  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      @NonNull final APIGatewayProxyRequestEvent input,
      @NonNull final Context context) {
    try {
      final String state = lambdaHttpValueExtractor.getAllQueryParams(
          input.getMultiValueQueryStringParameters(),
          input.getQueryStringParameters(),
          Constants.STATE_QUERY_PARAM).get(0);

      // Extract the cookie header looking for the state cookie
      final List<String> savedState = lambdaHttpCookieExtractor.getAllQueryParams(
          input.getMultiValueHeaders(),
          input.getHeaders(),
          Constants.STATE_COOKIE
      );

      if (!savedState.contains(state)) {
        final APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();
        apiGatewayProxyResponseEvent.setStatusCode(400);
        apiGatewayProxyResponseEvent.setBody("Invalid state parameter");
      }

      final String code = lambdaHttpValueExtractor.getAllQueryParams(
          input.getMultiValueQueryStringParameters(),
          input.getQueryStringParameters(),
          Constants.CODE_QUERY_PARAM).get(0);

      final OauthResponse response = gitHubOauth.accessToken(
          clientId,
          clientSecret,
          code,
          clientRedirect);

      if (!StringUtils.isAllBlank(response.getError())) {
        throw new Exception(response.getError() + "\n" + response.getErrorDescription());
      }

      final APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();
      apiGatewayProxyResponseEvent.setStatusCode(307);
      apiGatewayProxyResponseEvent.setHeaders(new ImmutableMap.Builder<String, String>()
          .put("Location", clientRedirect)
          .build());
      apiGatewayProxyResponseEvent.setMultiValueHeaders(
          new ImmutableMap.Builder<String, List<String>>()
              .put("Set-Cookie", new ImmutableList.Builder<String>()
                  .add(Constants.SESSION_COOKIE + "="
                      + cryptoUtils.encrypt(
                      response.getAccessToken(),
                      githubEncryption,
                      githubSalt))
                  .add(Constants.STATE_COOKIE
                      + "=deleted; expires=Thu, 01 Jan 1970 00:00:00 GMT")
                  .build())
              .build());
      return apiGatewayProxyResponseEvent;

    } catch (final Exception ex) {
      Log.error("GitHubOauthProxy-Exchange-GeneralError: " + ex);
      final APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();
      apiGatewayProxyResponseEvent.setStatusCode(500);
      apiGatewayProxyResponseEvent.setBody("An internal error was detected");
      return apiGatewayProxyResponseEvent;
    }
  }
}
