package com.octopus.jenkins.domain.entities;

import lombok.Data;

/**
 * The data returned by the GitHub public email endpoint.
 */
@Data
public class GitHubEmail {
  private String email;
}
