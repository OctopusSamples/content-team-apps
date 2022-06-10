package com.octopus.githubrepo.domain.cognito.impl;

import com.octopus.features.MicroserviceNameFeature;
import com.octopus.githubrepo.GlobalConstants;
import com.octopus.githubrepo.domain.cognito.CognitoAccessTokenGenerator;
import com.octopus.githubrepo.infrastructure.clients.CognitoClient;
import io.quarkus.logging.Log;
import io.vavr.control.Try;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

@ApplicationScoped
public class CognitoAccessTokenGeneratorImpl implements CognitoAccessTokenGenerator {
  private static long expiry;
  private static String accessToken;

  @ConfigProperty(name = "cognito.client-id")
  Optional<String> cognitoClientId;

  @ConfigProperty(name = "cognito.client-secret")
  Optional<String> cognitoClientSecret;

  @RestClient
  CognitoClient cognitoClient;

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  @Override
  public Try<String> getAccessToken() {
    if (!StringUtils.isEmpty(accessToken) && new Date().getTime() < expiry) {
      return Try.of(() -> accessToken);
    }

    if (!(cognitoClientId.isPresent() && cognitoClientSecret.isPresent())) {
      return Try.failure(new Exception("Cognito client ID or secret were not set"));
    }

    return Try.of(() -> cognitoClient.getToken(
            "Basic " + Base64.getEncoder().encodeToString((cognitoClientId.get() + ":" + cognitoClientSecret.get()).getBytes()),
            GlobalConstants.CLIENT_CREDENTIALS,
            cognitoClientId.get(),
            GlobalConstants.AUDIT_SCOPE))
        // We expect to see an access token. Fail if the value is empty.
        .filter(oauth -> StringUtils.isNotEmpty(oauth.getAccessToken()))
        // We can reuse a token for an hour, but we set the expiry 10 mins before just to be safe.
        .mapTry(oauth -> {
          accessToken = oauth.getAccessToken();
          expiry = new Date().getTime() + ((long) oauth.getExpiresIn() * 1000) - (10 * 60 * 1000);
          return accessToken;
        })
        // Log the failure, and try to log the response body
        .onFailure(ClientWebApplicationException.class, e -> Log.error(
            microserviceNameFeature.getMicroserviceName() + "-Cognito-LoginFailed "
                + Try.of(() -> e.getResponse().readEntity(String.class)).getOrElse("")));
  }
}
