package com.octopus.githubrepo.domain.utils.impl;

import com.octopus.exceptions.InvalidInputException;
import com.octopus.githubrepo.domain.utils.ScopeVerifier;
import com.octopus.githubrepo.infrastructure.clients.GitHubClient;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * An implementation of ScopeVerifier that queries the rate limit endpoint and extracts the scopes
 * from the returned headers.
 */
@ApplicationScoped
public class ScopeVerifierImpl implements ScopeVerifier {

  @RestClient
  GitHubClient gitHubClient;

  @Override
  public void verifyScopes(@NonNull final String decryptedGithubToken) {
    try (final Response response = gitHubClient.checkRateLimit("token " + decryptedGithubToken)) {
      final List<String> scopes =
          Arrays.stream(response
                  .getHeaderString("X-OAuth-Scopes")
                  .split(","))
              .map(String::trim)
              .collect(Collectors.toList());

      if (!scopes.contains("workflow")) {
        throw new InvalidInputException("GitHub token did not have the workflow scope");
      }

      if (!scopes.contains("repo")) {
        throw new InvalidInputException("GitHub token did not have the repo scope");
      }
    }
  }
}
