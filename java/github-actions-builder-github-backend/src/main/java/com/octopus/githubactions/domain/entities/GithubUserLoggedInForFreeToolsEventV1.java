package com.octopus.githubactions.domain.entities;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * The entity sent to the commercial team service bus.
 *
 * <p>See https://gist.github.com/archennz/956dd160b175ac77816af6255cb704b5
 */
@Data
@Builder
public class GithubUserLoggedInForFreeToolsEventV1 {
  private Map<String, String> UtmParameters;
  private String emailAddress;
  private String programmingLanguage;
  private String gitHubUsername;
  private String firstName;
  private String lastName;
}
