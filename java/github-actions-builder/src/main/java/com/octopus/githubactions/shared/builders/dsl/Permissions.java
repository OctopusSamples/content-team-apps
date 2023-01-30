package com.octopus.githubactions.shared.builders.dsl;

import lombok.Builder;
import lombok.Data;

/** Represents the permissions assigned to the workflow. */
@Data
@Builder
public class Permissions {
  private String idToken;
  private String contents;
  private String checks;
}
