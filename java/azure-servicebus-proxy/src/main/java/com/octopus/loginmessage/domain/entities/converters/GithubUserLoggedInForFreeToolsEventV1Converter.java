package com.octopus.loginmessage.domain.entities.converters;

import com.octopus.loginmessage.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.loginmessage.domain.entities.GithubUserLoggedInForFreeToolsEventV1Upstream;

/**
 * Represents a service that converts messages from API to the upstream versions.
 */
public interface GithubUserLoggedInForFreeToolsEventV1Converter {

  /**
   * Convert the API message to the upstream message.
   *
   * @param api The message received from the API.
   * @return The message sent to upstream services.
   */
  GithubUserLoggedInForFreeToolsEventV1Upstream from(GithubUserLoggedInForFreeToolsEventV1 api);
}
