package com.octopus.loginmessage.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * The entity sent to the commercial team service bus. This entity has no ID field, and serializes
 * to PascalCase.
 *
 * <p>See https://gist.github.com/archennz/956dd160b175ac77816af6255cb704b5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GithubUserLoggedInForFreeToolsEventV1Upstream {

  @JsonProperty("UtmParameters")
  private Map<String, String> utmParameters;
  @JsonProperty("EmailAddress")
  private String emailAddress;
  @JsonProperty("ProgrammingLanguage")
  private String programmingLanguage;
  @JsonProperty("GitHubUsername")
  private String gitHubUsername;
  @JsonProperty("FirstName")
  private String firstName;
  @JsonProperty("LastName")
  private String lastName;
}
