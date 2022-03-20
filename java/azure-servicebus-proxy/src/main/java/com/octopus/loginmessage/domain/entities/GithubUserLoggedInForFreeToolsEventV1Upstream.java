package com.octopus.loginmessage.domain.entities;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * The entity sent to the commercial team service bus.
 *
 * <p>See https://gist.github.com/archennz/956dd160b175ac77816af6255cb704b5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GithubUserLoggedInForFreeToolsEventV1Upstream {

  private Map<String, String> utmParameters;
  private String emailAddress;
  private String programmingLanguage;
  private String gitHubUsername;
  private String firstName;
  private String lastName;

  public static GithubUserLoggedInForFreeToolsEventV1Upstream fromApi(@NonNull final GithubUserLoggedInForFreeToolsEventV1 event) {
    return GithubUserLoggedInForFreeToolsEventV1Upstream.builder()
        .utmParameters(event.getUtmParameters())
        .programmingLanguage(event.getProgrammingLanguage())
        .gitHubUsername(event.getGitHubUsername())
        .firstName(event.getFirstName())
        .lastName(event.getLastName())
        .build();
  }
}
