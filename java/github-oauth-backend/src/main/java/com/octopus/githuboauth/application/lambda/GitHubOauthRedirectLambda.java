package com.octopus.githuboauth.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.common.collect.ImmutableMap;
import com.octopus.githuboauth.domain.oauth.OauthResponse;
import com.octopus.githuboauth.infrastructure.client.GitHubOauth;
import com.octopus.githuboauth.infrastructure.repositories.OauthStateRepository;
import com.octopus.lambda.ProxyResponse;
import com.octopus.lambda.QueryParamExtractor;
import io.vavr.control.Try;
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

  private static final String CODE_QUERY_PARAM = "code";
  private static final String STATE_QUERY_PARAM = "state";

  @Inject
  OauthStateRepository oauthStateRepository;

  @ConfigProperty(name = "github.client.redirect")
  String clientRedirect;

  @ConfigProperty(name = "github.client.id")
  String clientId;

  @ConfigProperty(name = "github.client.secret")
  String clientSecret;

  @ConfigProperty(name = "github.encryption")
  String githubEncryption;

  @Inject
  QueryParamExtractor queryParamExtractor;

  @RestClient
  GitHubOauth gitHubOauth;

  @Override
  public ProxyResponse handleRequest(@NonNull final APIGatewayProxyRequestEvent input,
      @NonNull final Context context) {
    final String state = queryParamExtractor.getAllQueryParams(
        input.getMultiValueQueryStringParameters(),
        input.getQueryStringParameters(),
        STATE_QUERY_PARAM).get(0);

    if (oauthStateRepository.findOne(state) == null) {
      return new ProxyResponse("400", "Invalid state parameter");
    }

    try {
      final String code = queryParamExtractor.getAllQueryParams(
          input.getMultiValueQueryStringParameters(),
          input.getQueryStringParameters(),
          CODE_QUERY_PARAM).get(0);

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
              .build());
    } finally {
      // Clean up the state, and ignore any errors
      Try.run(() -> oauthStateRepository.delete(state));
    }
  }
}
