package com.octopus.loginmessage.domain.entities;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The entity sent to the commercial team service bus.
 *
 * <p>See https://gist.github.com/archennz/956dd160b175ac77816af6255cb704b5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Type("loginmessage")
public class GithubUserLoggedInForFreeToolsEventV1 {
  @Id
  private String id;
  private Map<String, String> utmParameters;
  private String emailAddress;
  private String programmingLanguage;
  private String gitHubUsername;
  private String firstName;
  private String lastName;
}
