package com.octopus.githuboauth.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.octopus.PipelineConstants;
import com.octopus.encryption.CryptoUtils;
import com.octopus.githuboauth.OauthBackendConstants;
import com.octopus.githuboauth.domain.handlers.GitHubOauthRedirect;
import com.octopus.githuboauth.domain.handlers.SimpleResponse;
import com.octopus.githuboauth.domain.oauth.OauthResponse;
import com.octopus.githuboauth.infrastructure.client.GitHubOauth;
import com.octopus.http.CookieDateUtils;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import com.octopus.lambda.ProxyResponse;
import io.quarkus.logging.Log;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;


/**
 * This lambda handles the conversion of a code to an access token.
 * https://docs.github.com/en/developers/apps/building-github-apps/identifying-and-authorizing-users-for-github-apps#2-users-are-redirected-back-to-your-site-by-github
 */
@Named("accessToken")
public class GitHubOauthRedirectLambda implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @Inject
  LambdaHttpValueExtractor lambdaHttpValueExtractor;

  @Inject
  LambdaHttpCookieExtractor lambdaHttpCookieExtractor;

  @Inject
  GitHubOauthRedirect gitHubOauthRedirect;

  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      @NonNull final APIGatewayProxyRequestEvent input,
      @NonNull final Context context) {

    final String state = lambdaHttpValueExtractor.getAllQueryParams(
        input,
        OauthBackendConstants.STATE_QUERY_PARAM).get(0);

    final List<String> savedState = lambdaHttpCookieExtractor.getAllCookieValues(
        input,
        OauthBackendConstants.STATE_COOKIE);

    final String code = lambdaHttpValueExtractor.getAllQueryParams(
        input,
        OauthBackendConstants.CODE_QUERY_PARAM).get(0);

    final SimpleResponse response = gitHubOauthRedirect.oauthRedirect(state, savedState, code);

    return new APIGatewayProxyResponseEvent()
        .withStatusCode(response.getCode())
        .withMultiValueHeaders(response.getMultiValueHeaders())
        .withHeaders(response.getHeaders())
        .withBody(response.getBody());
  }
}
