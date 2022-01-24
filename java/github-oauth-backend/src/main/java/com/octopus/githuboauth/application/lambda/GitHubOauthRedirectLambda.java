package com.octopus.githuboauth.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.common.collect.ImmutableMap;
import com.octopus.encryption.CryptoUtils;
import com.octopus.githuboauth.Constants;
import com.octopus.githuboauth.domain.oauth.OauthResponse;
import com.octopus.githuboauth.infrastructure.client.GitHubOauth;
import com.octopus.lambda.LambdaHttpValueExtractor;
import com.octopus.lambda.ProxyResponse;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;


/**
 * This lambda handles the conversion of a code to an access token. https://docs.github.com/en/developers/apps/building-github-apps/identifying-and-authorizing-users-for-github-apps#2-users-are-redirected-back-to-your-site-by-github
 */
@Named("accessToken")
public class GitHubOauthRedirectLambda implements
    RequestHandler<APIGatewayProxyRequestEvent, ProxyResponse> {

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

  @RestClient
  GitHubOauth gitHubOauth;

  @Inject
  CryptoUtils cryptoUtils;

  @Override
  public ProxyResponse handleRequest(@NonNull final APIGatewayProxyRequestEvent input,
      @NonNull final Context context) {
    final String state = lambdaHttpValueExtractor.getAllQueryParams(
        input.getMultiValueQueryStringParameters(),
        input.getQueryStringParameters(),
        Constants.STATE_QUERY_PARAM).get(0);

    final List<String> savedState = lambdaHttpValueExtractor.getAllQueryParams(
        input.getMultiValueHeaders(),
        input.getHeaders(),
        Constants.STATE_COOKIE);

    if (!savedState.contains(state)) {
      return new ProxyResponse("400", "Invalid state parameter");
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

    return new ProxyResponse(
        "307",
        null,
        new ImmutableMap.Builder<String, String>()
            .put("Location", clientRedirect)
            .put("Set-Cookie", Constants.SESSION_COOKIE + "=" + cryptoUtils.encrypt(
                response.getAccessToken(),
                githubEncryption,
                githubSalt))
            .put("Set-Cookie", Constants.STATE_COOKIE + "=deleted; expires=Thu, 01 Jan 1970 00:00:00 GMT")
            .build());

  }
}
