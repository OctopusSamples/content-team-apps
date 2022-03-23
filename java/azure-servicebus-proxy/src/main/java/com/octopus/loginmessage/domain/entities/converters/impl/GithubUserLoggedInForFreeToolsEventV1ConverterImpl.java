package com.octopus.loginmessage.domain.entities.converters.impl;

import com.octopus.loginmessage.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.loginmessage.domain.entities.GithubUserLoggedInForFreeToolsEventV1Upstream;
import com.octopus.loginmessage.domain.entities.converters.GithubUserLoggedInForFreeToolsEventV1Converter;
import javax.enterprise.context.ApplicationScoped;

/**
 * An implementation of GithubUserLoggedInForFreeToolsEventV1Converter to convert API and upstream
 * messages.
 */
@ApplicationScoped
public class GithubUserLoggedInForFreeToolsEventV1ConverterImpl implements
    GithubUserLoggedInForFreeToolsEventV1Converter {

  @Override
  public GithubUserLoggedInForFreeToolsEventV1Upstream from(
      GithubUserLoggedInForFreeToolsEventV1 api) {
    return GithubUserLoggedInForFreeToolsEventV1Upstream.builder()
        .emailAddress(api.getEmailAddress())
        .utmParameters(api.getUtmParameters())
        .programmingLanguage(api.getProgrammingLanguage())
        .gitHubUsername(api.getGitHubUsername())
        .firstName(api.getFirstName())
        .lastName(api.getLastName())
        .build();
  }
}
